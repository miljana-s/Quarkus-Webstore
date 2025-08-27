package com.example.webstore.resource;

import com.example.webstore.dto.ProductRequest;
import com.example.webstore.model.Category;
import com.example.webstore.model.Product;
import com.example.webstore.security.JwtService;
import com.example.webstore.service.ProductService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ProductResourceTest {

    @Inject ProductService productService;

    private String sellerJwt() {
        return JwtService.generateToken(10L, "seller1", Set.of("SELLER"));
    }

    private Long anyExistingProductId() {
        Product p = Product.findAll().firstResult();
        assertThat(p).as("Mora postojati bar jedan proizvod (StartupDataLoader)").isNotNull();
        return p.id;
    }

    private Long anyCategoryId() {
        Category c = Category.findAll().firstResult();
        assertThat(c).as("Mora postojati bar jedna kategorija (StartupDataLoader)").isNotNull();
        return c.id;
    }


    @Test
    void GET_products_view_returnsHtml() {
        given()
                .when().get("/products/view")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(anyOf(containsString("Products"),
                        containsString("product_list")));
    }

    @Test
    void GET_products_search_returnsHtml() {
        given()
                .when().get("/products/search?term=Ring")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("Products"));
    }

    @Test
    void GET_products_type_returnsHtml() {
        Long catId = anyCategoryId();

        given()
                .when().get("/products/type/" + catId + "?size=12")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(containsString("Products"));
    }

    @Test
    void GET_overview_existingProduct_returnsHtml() {
        Long id = anyExistingProductId();

        given()
                .when().get("/products/overview/" + id)
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(anyOf(containsString("selectedProduct"),
                        containsString("View product"),
                        containsString("Price")));
    }


    @Test
    void GET_edit_withSellerRole_rendersHtml() {
        Long id = anyExistingProductId();

        given()
                .cookie("JWT", sellerJwt())
                .when().get("/products/edit/" + id)
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(anyOf(containsString("Update Product"),
                        containsString("selectedProduct"),
                        containsString("categories")));
    }

    @Test
    void POST_update_withSellerRole_redirectsWithSuccess() {
        Long id = anyExistingProductId();
        Long catId = anyCategoryId();

        String body = "id=" + id +
                "&name=Test+Name" +
                "&price=11.5" +
                "&quantity=9" +
                "&image=url" +
                "&categoryId=" + catId;

        given()
                .redirects().follow(false)
                .cookie("JWT", sellerJwt())
                .contentType("application/x-www-form-urlencoded")
                .body(body)
                .when().post("/products/update")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", containsString("/products/edit/" + id + "?success=1"));
    }

    @Test
    void GET_addProduct_withSellerRole_rendersHtml() {
        given()
                .cookie("JWT", sellerJwt())
                .when().get("/products/add-product")
                .then()
                .statusCode(200)
                .contentType(containsString("text/html"))
                .body(anyOf(containsString("Add Product"),
                        containsString("selectedProduct"),
                        containsString("categories")));
    }

    @Test
    void POST_addProduct_withSellerRole_redirectsWithSuccess() {
        Long catId = anyCategoryId();

        String body = "name=Temp+Prod" +
                "&price=12.34" +
                "&quantity=7" +
                "&image=http%3A%2F%2Fimg" +
                "&categoryId=" + catId;

        given()
                .redirects().follow(false)
                .cookie("JWT", sellerJwt())
                .contentType("application/x-www-form-urlencoded")
                .body(body)
                .when().post("/products/add-product")
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", containsString("/products/add-product?success=true"));
    }

    @Test
    void POST_delete_withSellerRole_redirectsToView_successPath() {
        Long catId = anyCategoryId();

        ProductRequest req = new ProductRequest();
        req.setName("ToDelete-Res");
        req.setPrice(1.0);
        req.setQuantity(1);
        req.setImage("img");
        req.setCategoryId(catId);
        productService.createProduct(req);

        Product tmp = Product.find("name", "ToDelete-Res").firstResult();
        assertThat(tmp).isNotNull();

        given()
                .redirects().follow(false)
                .cookie("JWT", sellerJwt())
                .when().post("/products/delete/" + tmp.id)
                .then()
                .statusCode(anyOf(is(302), is(303)))
                .header("Location", containsString("/products/view?success=true"));
    }
}
