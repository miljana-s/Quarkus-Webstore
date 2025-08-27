package com.example.webstore.resource;

import com.example.webstore.dto.UserDTO;
import com.example.webstore.model.User;
import com.example.webstore.security.JwtService;
import com.example.webstore.service.CartService;
import com.example.webstore.service.UserService;
import io.quarkus.qute.Template;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.net.URI;
import java.util.Set;

@Path("/webstore")
public class UserResource {

    @Inject Template login;
    @Inject Template register;
    @Inject Template profile;

    @Inject UserService userService;
    @Inject CartService cartService;

    @Inject JsonWebToken jwt;

    @ConfigProperty(name = "webstore.jwt.cookie.max-age", defaultValue = "43200")
    int jwtCookieMaxAge;

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public String showLoginPage(@QueryParam("error") String error) {
        return login.data("displayLoginError", error != null ? "Invalid credentials" : null).render();
    }

    @GET
    @Path("/register")
    @Produces(MediaType.TEXT_HTML)
    public String showRegisterPage(@QueryParam("error") String error) {
        return register.data("displayRegisterError", error != null).render();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response handleLogin(@FormParam("username") String username,
                                @FormParam("password") String password) {

        User user = userService.loginAndGet(username, password);
        if (user == null) {
            return Response.seeOther(URI.create("/webstore/login?error=1")).build();
        }


        Set<String> roles = Set.of(user.role != null ? user.role.name : "CUSTOMER");

        String token = JwtService.generateToken(user.id, user.username, roles);


        NewCookie jwtCookie = new NewCookie(
                "JWT",
                token,
                "/",
                null,
                "Auth token",
                jwtCookieMaxAge,
                false,
                true
        );


        return Response.seeOther(URI.create("/products/view"))
                .cookie(jwtCookie)
                .build();
    }

    @GET
    @Path("/logout")
    public Response logout() {
        NewCookie clear = new NewCookie("JWT", "", "/", null, "Auth token", 0, false, true);
        return Response.seeOther(URI.create("/"))
                .cookie(clear)
                .build();
    }


    private String roleForTemplate() {
        try { return jwt.getGroups().contains("SELLER") ? "SELLER" : "CUSTOMER"; }
        catch (Exception e) { return "CUSTOMER"; }
    }

    @GET @Path("/profile") @Authenticated @Produces(MediaType.TEXT_HTML)
    public String getProfilePage(@QueryParam("updated") boolean updated, @QueryParam("warning") boolean warning) {
        Long currentUserId = Long.valueOf(jwt.getSubject());
        var userDTO = userService.getUserById(currentUserId);
        return profile
                .data("user", userDTO)
                .data("showSuccMsg", updated)
                .data("showWarnMsg", warning)
                .data("user_role", roleForTemplate())
                .data("cart_count", cartService.countItems(currentUserId))
                .render();
    }

    @POST
    @Path("/profile")
    @Authenticated
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String updateProfile(@Context UriInfo uriInfo, MultivaluedMap<String, String> formParams) {
        Long currentUserId = Long.valueOf(jwt.getSubject());

        UserDTO dto = new UserDTO();
        dto.id = currentUserId;
        dto.firstName = formParams.getFirst("firstName");
        dto.lastName = formParams.getFirst("lastName");
        dto.email = formParams.getFirst("email");
        dto.phone = formParams.getFirst("phone");
        dto.address = formParams.getFirst("address");
        dto.city = formParams.getFirst("city");

        boolean updated = userService.updateUser(dto);
        return getProfilePage(updated, !updated);
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    //@Produces(MediaType.TEXT_PLAIN)
    public String handleRegister(@FormParam("firstName") String firstName,
                                 @FormParam("lastName") String lastName,
                                 @FormParam("username") String username,
                                 @FormParam("password") String password,
                                 @FormParam("email") String email,
                                 @FormParam("phone") String phone,
                                 @FormParam("address") String address,
                                 @FormParam("city") String city) {


        String error = userService.register(firstName, lastName, username, password,
                email, phone, address, city);

        if (error != null) {
            return register
                    .data("displayRegisterError", error)
                    .render();
        }

        return login.data("displayLoginError", null).render();
    }

//    @POST
//    @Path("/register-api")
//    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
//    @Produces(MediaType.TEXT_PLAIN)
//    @Blocking
//    public String registerApi(@FormParam("firstName") String firstName,
//                              @FormParam("lastName") String lastName,
//                              @FormParam("username") String username,
//                              @FormParam("password") String password,
//                              @FormParam("email") String email,
//                              @FormParam("phone") String phone,
//                              @FormParam("address") String address,
//                              @FormParam("city") String city,
//                              @FormParam("role_id") Long roleId) {
//        if (roleId == null) roleId = 1L;
//        String err = userService.register(firstName, lastName, username, password,
//                email, phone, address, city, roleId);
//        return err == null ? "OK" : "DUPLICATE_OR_ERROR";
//    }



}
