package com.example.webstore.service;

import com.example.webstore.dto.CartItemRequest;
import com.example.webstore.model.Cart;
import com.example.webstore.model.CartItem;
import com.example.webstore.model.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class CartItemService {

    @Transactional
    public CartItem addItemToCart(Cart cart, CartItemRequest request) {
        Product product = Product.findById(request.productId);
        if (product == null) {
            throw new WebApplicationException("Product not found", 404);
        }


        CartItem existingItem = CartItem.find("cart.id = ?1 and product.id = ?2", cart.id, product.id)
                .firstResult();

        if (existingItem != null) {
            existingItem.quantity += request.quantity;
            return existingItem;
        }

        CartItem item = new CartItem();
        item.cart = cart;
        item.product = product;
        item.quantity = request.quantity;
        item.persist();

        return item;
    }

    @Transactional
    public void removeItem(Long itemId) {
        CartItem item = CartItem.findById(itemId);
        if (item == null) {
            throw new WebApplicationException("CartItem not found", 404);
        }
        item.delete();
    }

    @Transactional
    public void increaseQuantity(Long itemId) {
        CartItem item = CartItem.findById(itemId);
        if (item != null) {
            item.quantity++;
        }
    }

    @Transactional
    public void decreaseQuantity(Long itemId) {
        CartItem item = CartItem.findById(itemId);
        if (item != null) {
            if (item.quantity > 1) {
                item.quantity--;
            } else {
                item.delete();
            }
        }
    }


}