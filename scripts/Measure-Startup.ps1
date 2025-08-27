
[CmdletBinding()]
param(
  [string]$Image            = "quarkus-webstore:native",
  [string]$NamePrefix       = "quarkus-native",
  [int]$Port                = 18080,
  [string]$ReadinessPath    = "/q/health/ready",
  [hashtable]$Env           = @{},
  [int]$Repetitions         = 5,
  [int]$PollIntervalMs      = 100,
  [int]$TimeoutSec          = 120,
  [switch]$NoMemProbe,
  [switch]$NoDockerStats,
  [string]$CsvOut           = ".\quarkus_native_startup.csv"
)

if (-not $Env -or $Env.Count -eq 0) {
  $Env = @{
    QUARKUS_DATASOURCE_USERNAME = "root";
    QUARKUS_DATASOURCE_PASSWORD = "1111";
    QUARKUS_DATASOURCE_JDBC_URL = "jdbc:mysql://host.docker.internal:3307/webstore_quarkus?useSSL=false&allowPublicKeyRetrieval=true&tcpKeepAlive=true&connectTimeout=10000&socketTimeout=60000"
  }
}

function Build-EnvArgs([hashtable]$h) {
  $args = @()
  foreach ($k in $h.Keys) { $args += @("-e","$k=$($h[$k])") }
  return ,$args
}

function StdDev([double[]]$vals) {
  if (-not $vals -or $vals.Count -lt 2) { return 0 }
  $avg = ($vals | Measure-Object -Average).Average
  $sumSq = 0.0
  foreach ($v in $vals) { $sumSq += [math]::Pow(($v - $avg), 2) }
  return [math]::Sqrt($sumSq / ($vals.Count - 1))
}

$results = @()
$envArgs = Build-EnvArgs $Env

for ($i = 1; $i -le $Repetitions; $i++) {
  $name = "$NamePrefix-$i"

  docker rm -f $name 2>$null | Out-Null

  $start = Get-Date
  $cid = docker run -d -p "$($Port):8080" --name $name $envArgs $Image

  Start-Sleep -Milliseconds 300
  if (-not (docker ps -q -f "name=$name")) {
    Write-Warning "[$i/$Repetitions] Container exited. Logs:"
    if ($cid) { docker logs $cid } else { docker logs $name }
    continue
  }

  $deadline = (Get-Date).AddSeconds($TimeoutSec)
  $ready = $false
  $url = "http://localhost:$Port$ReadinessPath"

  while ((Get-Date) -lt $deadline) {
    try {
      $r = Invoke-WebRequest -UseBasicParsing $url -TimeoutSec 1 -Proxy:$null
      if ($r.StatusCode -eq 200) { $ready = $true; break }
    } catch { }
    Start-Sleep -Milliseconds $PollIntervalMs
  }

  $elapsedMs = [math]::Round(((Get-Date) - $start).TotalMilliseconds)
  if (-not $ready) {
    Write-Warning "[$i/$Repetitions] TIMEOUT ($elapsedMs ms) - readiness did not return 200 within $TimeoutSec s"
  } else {
    Write-Host "[$i/$Repetitions] Startup: $elapsedMs ms ($url)" -ForegroundColor Green
  }

  if (-not $NoDockerStats) {
    Write-Host "docker stats (snapshot):"
    docker stats $name --no-stream
  }

$rssMB = $null; $vmsMB = $null
if (-not $NoMemProbe) {
  $vm = docker exec $name sh -lc "grep -E 'VmRSS|VmSize' /proc/1/status" 2>$null
  if (-not $vm) { $vm = docker exec $name cat /proc/1/status 2>$null }

  if ($vm) {
    foreach ($line in $vm) {
      if ($line -match 'VmRSS:\s+(\d+)\s+kB') { $rssMB = [math]::Round([int]$matches[1] / 1024.0, 1) }
      if ($line -match 'VmSize:\s+(\d+)\s+kB') { $vmsMB = [math]::Round([int]$matches[1] / 1024.0, 1) }
    }
    $dispRss = if ($rssMB -ne $null) { $rssMB } else { "n/a" }
    $dispVms = if ($vmsMB -ne $null) { $vmsMB } else { "n/a" }
    Write-Host ("VmRSS={0} MB, VmSize={1} MB" -f $dispRss, $dispVms)
  }
  else {
    $memLine = docker stats $name --no-stream --format "{{.MemUsage}}"
    if ($memLine -match '^\s*([0-9\.]+)\s*MiB') {
      $mib = [double]$matches[1]
      $rssMB = [math]::Round($mib * 1.048576, 1)
      Write-Host ("VmRSS≈{0} MB (from docker stats), VmSize=n/a" -f $rssMB)
    } else {
      Write-Host "VmRSS=n/a, VmSize=n/a"
    }
  }
}


  $results += [pscustomobject]@{
    Run        = $i
    StartupMs  = $elapsedMs
    Ready      = $ready
    VmRSS_MB   = $rssMB
    VmSize_MB  = $vmsMB
    Container  = $name
  }

  docker rm -f $name 2>$null | Out-Null
}

$ok = $results | Where-Object { $_.Ready -eq $true }
if ($ok.Count -gt 0) {
  $times = $ok | Select-Object -ExpandProperty StartupMs
  $avg   = [math]::Round(($times | Measure-Object -Average).Average, 1)
  $min   = ($times | Measure-Object -Minimum).Minimum
  $max   = ($times | Measure-Object -Maximum).Maximum
  $sd    = [math]::Round((StdDev $times), 1)

  Write-Host ""
  Write-Host "=== SUMMARY (ready=200) ===" -ForegroundColor Cyan
  Write-Host ("Runs OK: {0}/{1}" -f $ok.Count, $results.Count)
  Write-Host ("StartupMs: avg={0}  sd={1}  min={2}  max={3}" -f $avg, $sd, $min, $max)
} else {
  Write-Warning "No run reached readiness=200."
}

if ($CsvOut -and $CsvOut.Trim().Length -gt 0) {
  $results | Export-Csv -NoTypeInformation -Encoding UTF8 -Path $CsvOut
  Write-Host ('Results written to ' + $CsvOut)
}
