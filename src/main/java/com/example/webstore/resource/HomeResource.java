package com.example.webstore.resource;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class HomeResource {

    @Inject
    @Location("index.html")
    Template index;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String showHomePage() {
        return index.render();
    }
}
