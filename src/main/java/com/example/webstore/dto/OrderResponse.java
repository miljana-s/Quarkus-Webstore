package com.example.webstore.dto;

import com.example.webstore.model.Order;
import com.example.webstore.model.OrderStatus;
import io.quarkus.qute.TemplateData;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

@TemplateData
public class OrderResponse {
    public Long id;
    public Long userId;
    public String username;
    public LocalDateTime orderDate;
    public OrderStatus status;
    public List<OrderItemDTO> items;
    public double totalPrice;
    public String orderDatePretty;
    public String totalPricePretty;

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

        var nf = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        nf.setCurrency(Currency.getInstance("EUR"));
        this.totalPricePretty = nf.format(this.totalPrice);


        this.items = order.items.stream().map(item -> {
            OrderItemDTO dto = new OrderItemDTO(item.product.id, item.quantity);
            dto.productName = item.product.name;
            return dto;
        }).toList();


        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        this.orderDatePretty = (order.orderDate != null) ? order.orderDate.format(fmt) : "";
    }

}
