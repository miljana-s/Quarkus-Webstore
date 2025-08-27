package com.example.webstore.resource;

import com.example.webstore.model.User;
import com.example.webstore.security.JwtService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserResourceTest {

    @InjectMock
    com.example.webstore.service.CartService cartService;

    @Test
    void GET_loginPage_returnsHtml() {
        given()
                .when().get("/webstore/login")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"));
    }

    @Test
    void GET_registerPage_returnsHtml() {
        given()
                .when().get("/webstore/register")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"));
    }

    @Test
    void POST_register_success_rendersLoginPage() {
        String body = "firstName=Ana&lastName=Anic&username=ana2&password=secret123&email=ana2%40example.com";
        given()
                .contentType("application/x-www-form-urlencoded")
                .body(body)
                .when().post("/webstore/register")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("Login"));
    }

    @Test
    void POST_register_duplicateUsername_rendersRegisterWithError() {
        String body = "firstName=X&lastName=Y&username=user&password=secret123&email=newmail%40example.com";
        given()
                .contentType("application/x-www-form-urlencoded")
                .body(body)
                .when().post("/webstore/register")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(anyOf(containsString("already exists"), containsString("displayRegisterError")));
    }

    @Test
    void POST_login_invalid_redirectsToLoginWithError() {
        given()
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .body("username=nosuch&password=bad")
                .when().post("/webstore/login")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/webstore/login?error=1"));
    }

    @Test
    void POST_login_success_redirectsToProducts_andSetsJwtCookie() {
        given()
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .body("username=user&password=1234")
                .when().post("/webstore/login")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/products/view"))
                .header("Set-Cookie", containsString("JWT="));
    }

    @Test
    void GET_logout_clearsCookie_andRedirectsHome() {
        given()
                .redirects().follow(false)
                .when().get("/webstore/logout")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/"));
    }

    @Test
    void GET_profile_authenticated_rendersHtml() {
        Mockito.when(cartService.countItems(Mockito.anyLong())).thenReturn(0);

        User u = User.find("username","user").firstResult();
        assertThat(u).isNotNull();

        String token = JwtService.generateToken(u.id, u.username, Set.of("CUSTOMER"));

        given()
                .cookie("JWT", token)
                .when().get("/webstore/profile")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(anyOf(containsString(u.firstName), containsString("Profile")));
    }

    @Test
    void POST_profile_update_returnsUpdatedPage() {
        Mockito.when(cartService.countItems(Mockito.anyLong())).thenReturn(0);

        User u = User.find("username","user").firstResult();
        assertThat(u).isNotNull();

        String token = JwtService.generateToken(u.id, u.username, Set.of("CUSTOMER"));

        String body = "firstName=NewName&lastName=" + u.lastName +
                "&email=" + u.email +
                "&phone=555-111&address=A1&city=C1";

        given()
                .cookie("JWT", token)
                .contentType("application/x-www-form-urlencoded")
                .body(body)
                .when().post("/webstore/profile")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(anyOf(containsString("showSuccMsg"), containsString("updated")));
    }
}
