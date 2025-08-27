package com.example.webstore.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

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
