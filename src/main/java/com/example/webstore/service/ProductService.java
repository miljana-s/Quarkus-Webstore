package com.example.webstore.service;

import com.example.webstore.dto.ProductRequest;
import com.example.webstore.dto.ProductResponse;
import com.example.webstore.model.Category;
import com.example.webstore.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {

    public List<ProductResponse> listAll() {
        return Product.<Product>listAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ProductResponse findById(Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public void createProduct(ProductRequest request) {
        Product product = new Product();
        product.name = request.getName();
        product.price = request.getPrice();
        product.quantity = request.getQuantity();
        product.image = request.getImage();

        if (request.getCategoryId() != null) {
            Category category = Category.findById(request.getCategoryId());
            if (category != null) {
                product.category = category;
            }
        }

        product.persist();
    }



    public List<ProductResponse> searchByName(String term) {
        if (term == null || term.isBlank()) {
            return listAll();
        }

        return Product.<Product>list("LOWER(name) LIKE LOWER(?1)", "%" + term + "%")
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> filterByCategory(Long categoryId) {
        if (categoryId == null) {
            return listAll();
        }

        return Product.<Product>list("category.id = ?1", categoryId)
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void update(Long id, ProductRequest request) {
        Product product = Product.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }

        product.name = request.getName();
        product.price = request.getPrice();
        product.quantity = request.getQuantity();
        product.image = request.getImage();

        if (request.getCategoryId() != null) {
            Category category = Category.findById(request.getCategoryId());
            if (category == null) {
                throw new IllegalArgumentException("Category not found");
            }
            product.category = category;
        }

    }

    @Transactional
    public void deleteById(Long id) {
        Product product = Product.findById(id);
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        product.delete();
    }


}
