package com.example.webstore.resource;

import com.example.webstore.model.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@QuarkusTest
class OrderResourceTest {

    @Transactional
    Long ensureOrder() {
        return ensureOrderWithStatus(OrderStatus.UNCONFIRMED);
    }

    @Transactional
    Long ensureOrderWithStatus(OrderStatus status) {
        User seller = User.find("username", "seller").firstResult();
        if (seller == null) {
            seller = User.<User>findAll().firstResult();
        }
        Product product = Product.<Product>findAll().firstResult();

        Order o = new Order();
        o.user = seller;
        o.orderDate = LocalDateTime.now();
        o.status = status;
        o.totalPrice = product != null ? product.price : 0.0;

        if (product != null) {
            OrderItem item = new OrderItem();
            item.order = o;
            item.product = product;
            item.quantity = 1;
            o.items.add(item);
        }

        o.persist();
        return o.id;
    }

    // ---------- tests ----------

    @Test
    @TestSecurity(user = "seller", roles = {"SELLER"})
    void GET_orders_asSeller_returnsJson() {
        given()
                .when().get("/orders")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"));
    }

    @Test
    @TestSecurity(user = "seller", roles = {"SELLER"})
    void GET_orders_view_asSeller_rendersHtml() {
        given()
                .when().get("/orders/view")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"));
    }

    @Test
    @TestSecurity(user = "seller", roles = {"SELLER"})
    void POST_order_status_updates_andRedirects() {
        Long id = ensureOrderWithStatus(OrderStatus.UNCONFIRMED);

        given()
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .formParam("status", "CONFIRMED")
                .when()
                .post("/orders/{id}/status", id)
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/orders/view"));

        assertThat(((Order) Order.findById(id)).status).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @TestSecurity(user = "seller", roles = {"SELLER"})
    void POST_order_delete_redirects_and_removes() {
        Long id = ensureOrder();

        given()
                .redirects().follow(false)
                .contentType("application/x-www-form-urlencoded")
                .when()
                .post("/orders/{id}", id)
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", endsWith("/orders/view"));

        assertThat(Order.findById(id)).isNull();
    }
}
