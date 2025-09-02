package com.example.webstore.service;

import com.example.webstore.dto.OrderItemDTO;
import com.example.webstore.dto.OrderRequest;
import com.example.webstore.dto.OrderResponse;
import com.example.webstore.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        if (orderRequest.items == null || orderRequest.items.isEmpty()) {
            throw new WebApplicationException("Order must contain at least one item", 400);
        }

        Order order = new Order();
        order.user = user;
        order.orderDate = LocalDateTime.now();
        order.status = (orderRequest.status != null) ? orderRequest.status : OrderStatus.UNCONFIRMED;

        List<OrderItem> items = new ArrayList<>();
        double total = 0.0;

        for (OrderItemDTO itemDTO : orderRequest.items) {
            Product product = Product.findById(itemDTO.productId);
            if (product == null) {
                throw new WebApplicationException("Product with ID " + itemDTO.productId + " does not exist", 404);
            }
            if (itemDTO.quantity <= 0) {
                throw new WebApplicationException("Quantity must be greater than 0 for product " + itemDTO.productId, 400);
            }

            OrderItem item = new OrderItem();
            item.order = order;
            item.product = product;
            item.quantity = itemDTO.quantity;
            item.price = product.price;

            items.add(item);
            total += item.price * item.quantity;
        }

        order.items = items;
        order.totalPrice = total;

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
