package com.example.webstore.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
public class CartItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    public Cart cart;

    @ManyToOne
    @JoinColumn(name = "product_id")
    public Product product;

    public int quantity;
}
