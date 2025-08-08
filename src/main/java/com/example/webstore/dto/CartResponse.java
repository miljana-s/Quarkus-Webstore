package com.example.webstore.dto;

import com.example.webstore.model.Cart;

import java.util.List;
import java.util.stream.Collectors;

public class CartResponse {
    public Long id;
    public Long userId;
    public List<CartItemResponse> items;
    public double totalPrice;
    public int totalItems;

    public CartResponse(Cart cart) {
        this.id = cart.id;
        this.userId = cart.user.id;
        this.items = (cart.items == null) ? List.of()
                : cart.items.stream().map(CartItemResponse::new).collect(Collectors.toList());

        this.totalPrice = items.stream()
                .mapToDouble(i -> i.price * i.quantity)
                .sum();

        this.totalItems = items.stream()
                .mapToInt(i -> i.quantity)
                .sum();
    }
}
