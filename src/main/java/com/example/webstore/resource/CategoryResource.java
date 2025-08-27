package com.example.webstore.resource;

import com.example.webstore.dto.CategoryDTO;
import com.example.webstore.service.CategoryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    public List<CategoryDTO> getAll() {
        return categoryService.getAllCategories();
    }

    @POST
    public Response createCategory(String name) {
        try {
            categoryService.createCategory(name);
            return Response.status(Response.Status.CREATED).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid category name").build();
        }
    }
}
