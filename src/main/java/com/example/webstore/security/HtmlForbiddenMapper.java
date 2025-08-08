package com.example.webstore.security;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import jakarta.ws.rs.*;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import java.net.URI;

// 403: nema prava
@Provider
@Priority(Priorities.AUTHORIZATION + 1)
public class HtmlForbiddenMapper implements ExceptionMapper<jakarta.ws.rs.ForbiddenException> {
    @Context HttpHeaders headers;
    @Context UriInfo uri;

    @Override
    public Response toResponse(jakarta.ws.rs.ForbiddenException ex) {
        String accept = String.join(",", headers.getRequestHeader("Accept") == null ? java.util.List.of() : headers.getRequestHeader("Accept")).toLowerCase();
        String path = uri.getPath();
        if (!accept.contains("html") || path.startsWith("webstore/login") || path.startsWith("webstore/register") || path.startsWith("webstore/logout")) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        return Response.seeOther(URI.create("/webstore/login")).build();
    }
}
