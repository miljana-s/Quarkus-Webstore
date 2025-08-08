package com.example.webstore.dto;

import com.example.webstore.model.Order;
import com.example.webstore.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    public Long id;
    public Long userId;
    public String username;
    public LocalDateTime orderDate;
    public OrderStatus status;
    public List<OrderItemDTO> items;
    public double totalPrice;

    public OrderResponse() {
    }

    public OrderResponse(Long id, Long userId, LocalDateTime orderDate, OrderStatus status, List<OrderItemDTO> items, String username, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.items = items;
        this.username = username;
        this.totalPrice = totalPrice;
    }


    public OrderResponse(Order order) {
        this.id = order.id;
        this.userId = order.user.id;
        this.username = order.user.username;
        this.orderDate = order.orderDate;
        this.status = order.status;
        this.totalPrice = order.totalPrice != null ? order.totalPrice : 0.0;
        this.items = order.items.stream()
                .map(item -> new OrderItemDTO(item.product.id, item.quantity))
                .toList();
    }

}
