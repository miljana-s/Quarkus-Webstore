package com.example.webstore;

import com.example.webstore.dto.ProductRequest;
import com.example.webstore.dto.ProductResponse;
import com.example.webstore.service.CartService;
import com.example.webstore.service.CategoryService;
import com.example.webstore.service.ProductService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Path("/products")
public class ProductResource {

    @Inject ProductService productService;
    @Inject CategoryService categoryService;
    @Inject CartService cartService;

    @Inject Template products;

    @Inject @Location("product-overview.html")
    Template productOverview;

    @Inject @Location("update-product.html")
    Template updateProduct;

    @Inject @Location("add-product.html")
    Template addProduct;

    @Inject JsonWebToken jwt;


    private String roleForTemplate() {
        try {
            Set<String> groups = jwt.getGroups();
            if (groups != null && groups.contains("SELLER")) return "SELLER";
        } catch (Exception ignored) {}
        return "CUSTOMER";
    }


    @GET
    @Path("/view")
    @Produces(MediaType.TEXT_HTML)
    public String showProductsPage(@QueryParam("success") String success,
                                   @QueryParam("error") String error) {
        List<ProductResponse> productList = productService.listAll();
        return products.data("product_list", productList)
                .data("success", success != null)
                .data("error", error)
                .data("term", "")
                .data("categories", categoryService.getAllCategories())
                .data("user_role", roleForTemplate())
                .data("cart_count", cartCount())
                .render();
    }



    @GET
    @Path("/search")
    @Produces(MediaType.TEXT_HTML)
    public String searchProducts(@QueryParam("term") String term) {
        List<ProductResponse> result = productService.searchByName(term);
        return products.data("product_list", result)
                .data("success", false)
                .data("term", term)
                .data("error", null)
                .data("categories", categoryService.getAllCategories())
                .data("user_role", roleForTemplate())
                .data("cart_count", cartCount())
                .render();
    }


    @GET
    @Path("/type/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String filterByCategory(@PathParam("id") Long categoryId) {
        List<ProductResponse> result = productService.filterByCategory(categoryId);
        return products.data("product_list", result)
                .data("success", false)
                .data("term", "")
                .data("error", null)
                .data("categories", categoryService.getAllCategories())
                .data("user_role", roleForTemplate())
                .data("cart_count", cartCount())
                .render();
    }


    @GET
    @Path("/overview/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String showProductOverview(@PathParam("id") Long id) {
        var product = productService.findById(id);


        Long userId = null;
        try { userId = Long.valueOf(jwt.getSubject()); } catch (Exception ignored) {}

        return productOverview.data("selectedProduct", product)
                .data("user_id", userId)
                .data("user_role", roleForTemplate())
                .data("cart_count", cartCount())
                .render();
    }


    @GET
    @Path("/edit/{id}")
    @RolesAllowed("SELLER")
    @Produces(MediaType.TEXT_HTML)
    public String showEditForm(@PathParam("id") Long id, @QueryParam("success") String success) {
        ProductResponse product = productService.findById(id);
        return updateProduct
                .data("selectedProduct", product)
                .data("categories", categoryService.getAllCategories())
                .data("success", success != null)
                .data("user_role", roleForTemplate())
                .render();
    }


    @POST
    @Path("/update")
    @RolesAllowed("SELLER")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateProduct(@FormParam("id") Long id,
                                  @FormParam("name") String name,
                                  @FormParam("price") Double price,
                                  @FormParam("quantity") Integer quantity,
                                  @FormParam("image") String image,
                                  @FormParam("categoryId") Long categoryId) {

        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setPrice(price);
        request.setQuantity(quantity);
        request.setImage(image);
        request.setCategoryId(categoryId);

        productService.update(id, request);
        return Response.seeOther(URI.create("/products/edit/" + id + "?success=1")).build();
    }


    @POST
    @Path("/delete/{id}")
    @RolesAllowed("SELLER")
    public Response deleteProduct(@PathParam("id") Long id) {
        try {
            productService.deleteById(id);
            return Response.seeOther(URI.create("/products/view?success=true")).build();
        } catch (Exception e) {
            return Response.seeOther(URI.create("/products/view?error=linked")).build();
        }
    }



    @GET
    @Path("/add-product")
    @RolesAllowed("SELLER")
    @Produces(MediaType.TEXT_HTML)
    public String showAddProductForm(@QueryParam("success") boolean success) {
        return addProduct.data("selectedProduct", new ProductResponse())
                .data("categories", categoryService.getAllCategories())
                .data("showSuccessMessage", success)
                .data("success", success)
                .data("term", "")
                .data("user_role", roleForTemplate())
                .render();
    }


    @POST
    @Path("/add-product")
    @RolesAllowed("SELLER")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addProduct(@FormParam("name") String name,
                               @FormParam("price") double price,
                               @FormParam("quantity") int quantity,
                               @FormParam("image") String image,
                               @FormParam("categoryId") Long categoryId) {

        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setPrice(price);
        request.setQuantity(quantity);
        request.setImage(image);
        request.setCategoryId(categoryId);

        productService.createProduct(request);
        return Response.seeOther(URI.create("/products/add-product?success=true")).build();
    }

    private int cartCount() {
        try {
            if (jwt.getGroups().contains("CUSTOMER")) {
                return cartService.countItems(Long.valueOf(jwt.getSubject()));
            }
        } catch (Exception ignored) {}
        return 0;
    }
}
