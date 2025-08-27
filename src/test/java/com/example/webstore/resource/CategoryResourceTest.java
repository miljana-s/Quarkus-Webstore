package com.example.webstore.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CategoryResourceTest {

    @Test
    void GET_categories_returnsAllAsJson() {
        given()
                .when().get("/categories")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("size()", greaterThanOrEqualTo(6))
                .body("name", hasItems(
                        "NECKLACES", "BRACELETS", "EARRINGS",
                        "RINGS", "PENDANTS", "ANKLETS"
                ));
    }

    @Test
    void POST_categories_withInvalidName_returns400() {
        given()
                .contentType("application/json")
                .body("\"not-a-valid-enum\"")
                .when().post("/categories")
                .then()
                .statusCode(400)
                .contentType(anyOf(containsString("application/json"), containsString("text/plain")))
                .body(anyOf(containsString("Invalid category name"), not(emptyString())));
    }
}
