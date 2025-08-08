package com.example.webstore.dto;

import com.example.webstore.model.OrderStatus;
import java.util.List;

public class OrderRequest {
    public Long userId;
    public List<OrderItemDTO> items;
    public OrderStatus status;
    public Double totalPrice;

    public OrderRequest() {}

    public OrderRequest(Long userId, List<OrderItemDTO> items, OrderStatus status, Double totalPrice) {
        this.userId = userId;
        this.items = items;
        this.status = status;
        this.totalPrice = totalPrice;
    }
}
