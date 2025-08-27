
[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$Uri,
    [Parameter(Mandatory=$true)][string]$NewText,
    [int]$TimeoutSec = 120,
    [int]$IntervalMs = 100
)

$sw = [System.Diagnostics.Stopwatch]::StartNew()
while ($sw.Elapsed.TotalSeconds -lt $TimeoutSec) {
    try {
        $resp = Invoke-WebRequest -Uri $Uri -TimeoutSec 1 -Proxy:$null -Headers @{Accept='text/plain,application/json'}
        $body = $resp.Content
        if ($body -like "*$NewText*") {
            $sw.Stop()
            "{0}`t{1}" -f "hot_reload_seconds", ("{0:N3}" -f $sw.Elapsed.TotalSeconds)
            exit 0
        }
    } catch {

    }
    Start-Sleep -Milliseconds $IntervalMs
}
throw "Timeout ($TimeoutSec s) waiting for '$NewText' at $Uri"