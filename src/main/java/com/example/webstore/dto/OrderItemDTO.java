package com.example.webstore.dto;

import io.quarkus.qute.TemplateData;

@TemplateData
public class OrderItemDTO {
    public Long productId;
    public int quantity;
    public String productName;

    public OrderItemDTO() {}

    public OrderItemDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}