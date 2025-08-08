package com.example.webstore.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
public class CartItem extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "cart_id")
    public Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    public Product product;

    public int quantity;
}
