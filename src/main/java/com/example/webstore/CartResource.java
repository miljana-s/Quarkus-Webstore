package com.example.webstore;

import com.example.webstore.dto.CartItemRequest;
import com.example.webstore.dto.CartResponse;
import com.example.webstore.dto.OrderRequest;
import com.example.webstore.model.Cart;
import com.example.webstore.model.User;
import com.example.webstore.service.CartItemService;
import com.example.webstore.service.CartService;
import com.example.webstore.service.OrderService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;

@Path("/cart")
public class CartResource {

    @Inject CartService cartService;
    @Inject CartItemService cartItemService;
    @Inject OrderService orderService;

    @Inject @Location("cart.html")
    Template cartTemplate;

    @Inject JsonWebToken jwt;

    private Long currentUserId() {

        return Long.valueOf(jwt.getSubject());
    }

    private String roleForTemplate() {
        try { return jwt.getGroups().contains("SELLER") ? "SELLER" : "CUSTOMER"; }
        catch (Exception e) { return "CUSTOMER"; }
    }


    @GET
    @Path("/view")
    @RolesAllowed("CUSTOMER")
    @Produces(MediaType.TEXT_HTML)
    public String showCart(@QueryParam("success") Boolean success,
                           @QueryParam("empty") Boolean empty) {

        Long userId = currentUserId();
        CartResponse cart = cartService.getCartByUserId(userId);

        boolean isEmpty = (empty != null) ? empty : (cart.items == null || cart.items.isEmpty());
        double total = (cart.items == null) ? 0.0 :
                cart.items.stream().mapToDouble(i -> i.getSubtotal()).sum();

        return cartTemplate
                .data("cart", cart)
                .data("cartItems", cart.items)
                .data("totalPrice", total)
                .data("userId", userId)
                .data("success", Boolean.TRUE.equals(success))
                .data("empty", isEmpty)
                .data("user_role", roleForTemplate())
                .data("cart_count", cart.totalItems)
                .render();
    }


    @POST
    @Path("/add")
    @RolesAllowed("CUSTOMER")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response addToCart(@FormParam("productId") Long productId,
                              @FormParam("quantity") int quantity) {

        Long userId = currentUserId();

        CartItemRequest request = new CartItemRequest();
        request.setUserId(userId);
        request.setProductId(productId);
        request.setQuantity(quantity);


        Cart cart = Cart.find("user.id = ?1", userId).firstResult();
        if (cart == null) {
            User user = User.findById(userId);
            if (user == null) throw new WebApplicationException("User not found", 404);
            cart = new Cart();
            cart.user = user;
            cart.persist();
        }

        cartItemService.addItemToCart(cart, request);
        return Response.seeOther(URI.create("/products/overview/" + productId)).build();
    }

    @POST
    @Path("/remove/{itemId}")
    @RolesAllowed("CUSTOMER")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response removeItem(@PathParam("itemId") Long itemId) {
        cartItemService.removeItem(itemId);
        return Response.seeOther(URI.create("/cart/view")).build();
    }

    @GET
    @Path("/increase/{itemId}")
    @RolesAllowed("CUSTOMER")
    public Response increaseItem(@PathParam("itemId") Long itemId) {
        cartItemService.increaseQuantity(itemId);
        return Response.seeOther(URI.create("/cart/view")).build();
    }

    @GET
    @Path("/decrease/{itemId}")
    @RolesAllowed("CUSTOMER")
    public Response decreaseItem(@PathParam("itemId") Long itemId) {
        cartItemService.decreaseQuantity(itemId);
        return Response.seeOther(URI.create("/cart/view")).build();
    }

    @POST
    @Path("/confirm")
    @RolesAllowed("CUSTOMER")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response confirmOrder() {
        Long userId = currentUserId();
        if (cartService.countItems(userId) == 0) {
            return Response.seeOther(URI.create("/cart/view?empty=true")).build();
        }
        OrderRequest req = cartService.generateOrderRequest(userId);
        if (req == null) {
            return Response.seeOther(URI.create("/cart/view?empty=true")).build();
        }
        orderService.createOrderFromRequest(req);
        cartService.clearCart(userId);
        return Response.seeOther(URI.create("/cart/view?success=true")).build();
    }

    @POST
    @Path("/clear")
    @RolesAllowed("CUSTOMER")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response clearCart() {
        Long userId = currentUserId();
        cartService.clearCart(userId);
        return Response.seeOther(URI.create("/cart/view")).build();
    }
}
