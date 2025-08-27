package com.example.webstore.dto;

import com.example.webstore.model.Product;
import io.quarkus.qute.TemplateData;

@TemplateData
public class ProductResponse {

    public Long id;
    public String name;
    public Double price;
    public String image;
    public Integer quantity;
    public String categoryName;

    public ProductResponse() {}

    public ProductResponse(Long id, String name, Double price, String image, Integer quantity, String categoryName) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.image = image;
        this.quantity = quantity;
        this.categoryName = categoryName;
    }

    public ProductResponse(Product product) {
    }

    public static ProductResponse fromEntity(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.id);
        response.setName(product.name);
        response.setPrice(product.price);
        response.setImage(product.image);
        response.setQuantity(product.quantity);

        if (product.category != null) {
            response.setCategoryName(product.category.name.name());
        }

        return response;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }



}
