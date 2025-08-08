package com.example.webstore.dto;

public class OrderItemDTO {
    public Long productId;
    public int quantity;

    public OrderItemDTO() {}

    public OrderItemDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
