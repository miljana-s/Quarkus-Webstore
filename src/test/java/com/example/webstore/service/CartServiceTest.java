package com.example.webstore.service;

import com.example.webstore.dto.CartItemRequest;
import com.example.webstore.dto.CartResponse;
import com.example.webstore.dto.OrderRequest;
import com.example.webstore.model.Product;
import com.example.webstore.model.User;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class CartServiceTest {

    @Inject
    CartService cartService;

    Long userId;
    Long anyProductId;
    double anyProductPrice;

    @Transactional
    void loadIds() {
        User u = User.find("username", "user").firstResult();
        assertThat(u).as("user from StartupDataLoader").isNotNull();
        userId = u.id;

        Product p = Product.<Product>findAll().firstResult();
        assertThat(p).as("at least one product exists").isNotNull();
        anyProductId = p.id;
        anyProductPrice = p.price;
    }

    @BeforeEach
    void setup() {
        loadIds();
        cartService.clearCart(userId);
    }

    @Test
    void getCartByUserId_createsEmptyCart_ifMissing() {
        CartResponse cart = cartService.getCartByUserId(userId);
        assertThat(cart.userId).isEqualTo(userId);
        assertThat(cart.items).isEmpty();
        assertThat(cart.totalItems).isZero();
        assertThat(cart.totalPrice).isZero();
    }

    @Test
    void addToCart_addsAndAccumulatesQuantities() {
        CartItemRequest req = new CartItemRequest();
        req.setUserId(userId);
        req.setProductId(anyProductId);
        req.setQuantity(2);

        CartResponse afterFirst = cartService.addToCart(userId, req);
        assertThat(afterFirst.totalItems).isEqualTo(2);

        req.setQuantity(3);
        CartResponse afterSecond = cartService.addToCart(userId, req);
        assertThat(afterSecond.totalItems).isEqualTo(5);

        assertThat(cartService.countItems(userId)).isEqualTo(5);
        assertThat(afterSecond.totalPrice).isEqualTo(anyProductPrice * 5);
    }

    @Test
    void generateOrderRequest_returnsNullWhenEmpty() {
        cartService.clearCart(userId);
        OrderRequest req = cartService.generateOrderRequest(userId);
        assertThat(req).isNull();
    }

    @Test
    void generateOrderRequest_buildsRequestFromItems() {
        CartItemRequest add = new CartItemRequest();
        add.setUserId(userId);
        add.setProductId(anyProductId);
        add.setQuantity(4);
        cartService.addToCart(userId, add);

        OrderRequest req = cartService.generateOrderRequest(userId);
        assertThat(req).isNotNull();
        assertThat(req.userId).isEqualTo(userId);
        assertThat(req.items).hasSize(1);
        assertThat(req.items.get(0).productId).isEqualTo(anyProductId);
        assertThat(req.items.get(0).quantity).isEqualTo(4);
        assertThat(req.totalPrice).isEqualTo(anyProductPrice * 4);
    }

    @Test
    void clearCart_emptiesAllItems() {
        CartItemRequest add = new CartItemRequest();
        add.setUserId(userId);
        add.setProductId(anyProductId);
        add.setQuantity(1);
        cartService.addToCart(userId, add);

        assertThat(cartService.countItems(userId)).isEqualTo(1);

        cartService.clearCart(userId);
        assertThat(cartService.countItems(userId)).isZero();
    }
}
