package com.example.webstore.model;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
public class Product extends PanacheEntity {

    public String name;
    public double price;
    public int quantity;
    public String image;

    @ManyToOne
    @JoinColumn(name = "category_id")
    public Category category;


    public Product() {
    }


}
