package com.example.webstore.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart extends PanacheEntity {

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    public User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<CartItem> items = new ArrayList<>();
}
