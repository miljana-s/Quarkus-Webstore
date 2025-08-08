package com.example.webstore.service;

import com.example.webstore.dto.OrderItemDTO;
import com.example.webstore.dto.OrderRequest;
import com.example.webstore.dto.OrderResponse;
import com.example.webstore.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OrderService {

    public List<OrderResponse> listAllOrders() {
        return Order.<Order>listAll().stream()
                .map(OrderResponse::new)
                .toList();
    }


    @Transactional
    public Order createOrderFromRequest(OrderRequest orderRequest) {
        if (orderRequest.userId == null) {
            throw new WebApplicationException("User ID is required", 400);
        }

        User user = User.findById(orderRequest.userId);
        if (user == null) {
            throw new WebApplicationException("User does not exist", 404);
        }

        Order order = new Order();
        order.user = user;
        order.orderDate = LocalDateTime.now();

        order.status = orderRequest.status != null ? orderRequest.status : OrderStatus.UNCONFIRMED;

        order.totalPrice = orderRequest.totalPrice;

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDTO itemDTO : orderRequest.items) {
            Product product = Product.findById(itemDTO.productId);
            if (product == null) {
                throw new WebApplicationException("Product with ID " + itemDTO.productId + " does not exist", 404);
            }
            OrderItem item = new OrderItem();
            item.product = product;
            item.quantity = itemDTO.quantity;
            item.order = order;
            items.add(item);
        }

        order.items = items;
        order.persist();

        return order;
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = Order.findById(orderId);
        if (order == null) {
            throw new WebApplicationException("Order not found", 404);
        }
        order.status = status;
        return order;
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = Order.findById(id);
        if (order == null) {
            throw new WebApplicationException("Order not found", 404);
        }
        order.delete();
    }



}
