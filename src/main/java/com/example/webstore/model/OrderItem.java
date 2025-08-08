package com.example.webstore.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class OrderItem extends PanacheEntityBase {

    @EmbeddedId
    public OrderItemKey id = new OrderItemKey();

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    @JsonBackReference
    public Order order;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    public Product product;

    public int quantity;
}
