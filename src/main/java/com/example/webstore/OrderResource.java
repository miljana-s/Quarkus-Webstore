package com.example.webstore;

import com.example.webstore.dto.OrderResponse;
import com.example.webstore.model.OrderStatus;
import com.example.webstore.service.OrderService;
import io.quarkus.qute.Template;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.List;

@Path("/orders")
public class OrderResource {

    @Inject OrderService orderService;
    @Inject Template orders;
    @Inject JsonWebToken jwt;

    private String roleForTemplate() {
        try { return jwt.getGroups().contains("SELLER") ? "SELLER" : "CUSTOMER"; }
        catch (Exception e) { return "CUSTOMER"; }
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("SELLER")
    public List<OrderResponse> getAllOrders() {
        return orderService.listAllOrders();
    }


    @GET
    @Path("/view")
    @Produces(MediaType.TEXT_HTML)
    @RolesAllowed("SELLER")
    public String viewOrdersPage() {
        List<OrderResponse> orderResponses = orderService.listAllOrders();
        return orders.data("orders", orderResponses)
                .data("user_role", roleForTemplate())
                .render();
    }


    @POST
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed("SELLER")
    public Response updateOrderStatus(@PathParam("id") Long orderId, @FormParam("status") String status) {
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
        orderService.updateStatus(orderId, newStatus);
        return Response.seeOther(URI.create("/orders/view")).build();
    }


    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @RolesAllowed("SELLER")
    public Response deleteOrder(@PathParam("id") Long id) {
        orderService.deleteOrder(id);
        return Response.seeOther(URI.create("/orders/view")).build();
    }


}
