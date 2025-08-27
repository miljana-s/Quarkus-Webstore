package com.example.webstore.resource;

import com.example.webstore.model.Cart;
import com.example.webstore.model.Order;
import com.example.webstore.model.Product;
import com.example.webstore.model.User;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class CartResourceTest {

    Long userId;
    Long productId;
    String jwtCookie;


    @Transactional
    Long findUserId() {
        User u = User.find("username", "user").firstResult();
        assertThat(u).isNotNull();
        return u.id;
    }

    @Transactional
    Long firstProductId() {
        Product p = Product.<Product>findAll().firstResult();
        assertThat(p).isNotNull();
        return p.id;
    }

    String loginAndGetJwt(String username, String password) {
        return given()
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post("/webstore/login")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Set-Cookie", containsString("JWT="))
                .extract()
                .cookie("JWT");
    }

    @Transactional
    Long firstCartItemId(Long uId) {
        Cart cart = Cart.find("user.id = ?1", uId).firstResult();
        if (cart == null || cart.items == null || cart.items.isEmpty()) return null;
        return cart.items.get(0).id;
    }

    @Transactional
    int cartTotalQty(Long uId) {
        Cart cart = Cart.find("user.id = ?1", uId).firstResult();
        if (cart == null || cart.items == null) return 0;
        return cart.items.stream().mapToInt(ci -> ci.quantity).sum();
    }

    @Transactional
    long countOrdersForUser(Long uId) {
        return Order.find("user.id", uId).count();
    }

    @BeforeEach
    void setup() {
        userId = findUserId();
        productId = firstProductId();
        jwtCookie = loginAndGetJwt("user", "1234");
    }

    // ---------- tests ----------

    @Test
    void GET_cart_view_returnsHtml_forCustomer() {
        given()
                .cookie("JWT", jwtCookie)
                .when().get("/cart/view")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"));
    }

    @Test
    void POST_add_addsItem_andRedirectsToProductOverview() {
        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .formParam("productId", productId)
                .formParam("quantity", 2)
                .when()
                .post("/cart/add")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/products/overview/" + productId));

        assertThat(cartTotalQty(userId)).isEqualTo(2);
    }

    @Test
    void GET_increase_and_decrease_adjustQuantities() {

        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .formParam("productId", productId)
                .formParam("quantity", 1)
                .post("/cart/add")
                .then().statusCode(anyOf(is(302), is(303)));

        Long itemId = firstCartItemId(userId);
        assertThat(itemId).isNotNull();


        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .get("/cart/increase/{itemId}", itemId)
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/cart/view"));
        assertThat(cartTotalQty(userId)).isEqualTo(2);


        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .get("/cart/decrease/{itemId}", itemId)
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/cart/view"));
        assertThat(cartTotalQty(userId)).isEqualTo(1);


        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .get("/cart/decrease/{itemId}", itemId)
                .then()
                .statusCode(anyOf(is(302), is(303)));
        assertThat(cartTotalQty(userId)).isZero();
    }

    @Test
    void POST_removeItem_deletesLine_andRedirects() {

        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .formParam("productId", productId)
                .formParam("quantity", 1)
                .post("/cart/add")
                .then().statusCode(anyOf(is(302), is(303)));

        Long itemId = firstCartItemId(userId);
        assertThat(itemId).isNotNull();

        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .post("/cart/remove/{itemId}", itemId)
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/cart/view"));

        assertThat(cartTotalQty(userId)).isZero();
    }

    @Test
    void POST_clear_emptiesCart_andRedirects() {

        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .formParam("productId", productId)
                .formParam("quantity", 2)
                .post("/cart/add");

        assertThat(cartTotalQty(userId)).isEqualTo(2);

        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .post("/cart/clear")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/cart/view"));

        assertThat(cartTotalQty(userId)).isZero();
    }

    @Test
    void POST_confirm_createsOrder_clearsCart_andRedirectsWithSuccess() {
        long before = countOrdersForUser(userId);


        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .formParam("productId", productId)
                .formParam("quantity", 1)
                .post("/cart/add")
                .then().statusCode(anyOf(is(302), is(303)));

        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .post("/cart/confirm")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/cart/view?success=true"));

        assertThat(cartTotalQty(userId)).isZero();
        assertThat(countOrdersForUser(userId)).isEqualTo(before + 1);
    }

    @Test
    void POST_confirm_onEmptyCart_redirectsWithEmptyFlag() {

        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .post("/cart/clear");

        given()
                .cookie("JWT", jwtCookie)
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .post("/cart/confirm")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/cart/view?empty=true"));
    }
}
