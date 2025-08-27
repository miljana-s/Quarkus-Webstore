package com.example.webstore.service;

import com.example.webstore.dto.ProductRequest;
import com.example.webstore.dto.ProductResponse;
import com.example.webstore.model.Category;
import com.example.webstore.model.Product;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class ProductServiceTest {

    @Inject ProductService productService;

    private Long anyCategoryId() {
        Category c = Category.findAll().firstResult();
        assertThat(c).as("Mora postojati bar jedna kategorija (StartupDataLoader)").isNotNull();
        return c.id;
    }

    @Test
    void createProduct_persists_and_findById_mapsFields() {
        Long catId = anyCategoryId();

        ProductRequest req = new ProductRequest();
        req.setName("Srv-Create");
        req.setPrice(12.34);
        req.setQuantity(5);
        req.setImage("img-url");
        req.setCategoryId(catId);

        productService.createProduct(req);

        Product created = Product.find("name", "Srv-Create").firstResult();
        assertThat(created).isNotNull();

        ProductResponse r = productService.findById(created.id);
        assertThat(r).isNotNull();
        assertThat(r.getName()).isEqualTo("Srv-Create");
        assertThat(r.getPrice()).isEqualTo(12.34);
        assertThat(r.getQuantity()).isEqualTo(5);
        assertThat(r.getImage()).isEqualTo("img-url");
        assertThat(r.getCategoryName()).isNotBlank();
    }

    @Test
    void update_changesArePersisted() {
        Long catId = anyCategoryId();

        ProductRequest req = new ProductRequest();
        req.setName("ToUpdate");
        req.setPrice(1.0);
        req.setQuantity(1);
        req.setImage("img");
        req.setCategoryId(catId);
        productService.createProduct(req);

        Product p = Product.find("name", "ToUpdate").firstResult();
        assertThat(p).isNotNull();

        ProductRequest upd = new ProductRequest();
        upd.setName("Updated");
        upd.setPrice(9.99);
        upd.setQuantity(7);
        upd.setImage("img-new");
        upd.setCategoryId(catId);

        productService.update(p.id, upd);

        Product reloaded = QuarkusTransaction.requiringNew().call(() ->
                Product.<Product>findById(p.id)
        );
        assertThat(reloaded).isNotNull();
        assertThat(reloaded.name).isEqualTo("Updated");
        assertThat(reloaded.price).isEqualTo(9.99);
        assertThat(reloaded.quantity).isEqualTo(7);
        assertThat(reloaded.image).isEqualTo("img-new");
    }

    @Test
    void listPaged_returnsPage_withItems_andMeta() {
        var page = productService.listPaged(1, 5);
        assertThat(page).isNotNull();
        assertThat(page.items).isNotEmpty();
        assertThat(page.page).isEqualTo(1);
        assertThat(page.size).isEqualTo(5);
        assertThat(page.total).isGreaterThan(0);
        assertThat(page.totalPages).isGreaterThan(0);
    }

    @Test
    void searchByNamePaged_findsExpectedTerm() {
        Long catId = anyCategoryId();

        ProductRequest req = new ProductRequest();
        req.setName("SrvRing-X");
        req.setPrice(3.0);
        req.setQuantity(1);
        req.setImage("img");
        req.setCategoryId(catId);
        productService.createProduct(req);

        var result = productService.searchByNamePaged("ring", 1, 10);
        assertThat(result.items).anySatisfy(pr ->
                assertThat(pr.name).containsIgnoringCase("ring")
        );
    }

    @Test
    void filterByCategoryPaged_returnsItemsForThatCategory() {
        Long catId = anyCategoryId();

        ProductRequest req = new ProductRequest();
        req.setName("Srv-ByCat");
        req.setPrice(5.0);
        req.setQuantity(2);
        req.setImage("img");
        req.setCategoryId(catId);
        productService.createProduct(req);

        var result = productService.filterByCategoryPaged(catId, 1, 10);
        assertThat(result.items).isNotEmpty();
        assertThat(result.items).anySatisfy(pr -> assertThat(pr.categoryName).isNotBlank());
    }

    @Test
    void deleteById_removesProduct() {
        Long catId = anyCategoryId();

        ProductRequest req = new ProductRequest();
        req.setName("ToDelete-Srv");
        req.setPrice(2.0);
        req.setQuantity(2);
        req.setImage("img");
        req.setCategoryId(catId);
        productService.createProduct(req);

        Product p = Product.find("name", "ToDelete-Srv").firstResult();
        assertThat(p).isNotNull();

        productService.deleteById(p.id);

        Product gone = QuarkusTransaction.requiringNew().call(() ->
                Product.<Product>findById(p.id)
        );
        assertThat(gone).isNull();
    }
}
