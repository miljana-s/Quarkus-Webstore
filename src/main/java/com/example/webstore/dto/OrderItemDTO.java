package com.example.webstore.dto;

import com.example.webstore.model.OrderItem;
import io.quarkus.qute.TemplateData;

@TemplateData
public class OrderItemDTO {
    public Long productId;
    public int quantity;
    public String productName;
    public double price;
    public double lineTotal;

    public OrderItemDTO() {}

    public OrderItemDTO(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public static OrderItemDTO fromEntity(OrderItem oi) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.productId = oi.product.id;
        dto.quantity = oi.quantity;
        dto.productName = oi.product.name;
        dto.price = oi.price;
        dto.lineTotal = oi.price * oi.quantity;
        return dto;
    }
}
