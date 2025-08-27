package com.example.webstore.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hot")
public class HotResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hot() {
        return "v1";
    }
}
