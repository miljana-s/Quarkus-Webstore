package com.example.webstore.dto;

import com.example.webstore.model.CartItem;

public class CartItemResponse {
    public Long id;
    public Long productId;
    public String productName;
    public double price;
    public int quantity;
    public String image;

    public CartItemResponse(CartItem item) {
        this.id = item.id;
        this.productId = item.product.id;
        this.productName = item.product.name;
        this.price = item.product.price;
        this.quantity = item.quantity;
        this.image = item.product.image;
    }

    public double getSubtotal() {
        return price * quantity;
    }
}

