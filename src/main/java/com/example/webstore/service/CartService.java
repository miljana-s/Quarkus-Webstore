package com.example.webstore.service;

import com.example.webstore.dto.CartItemRequest;
import com.example.webstore.dto.CartResponse;
import com.example.webstore.dto.OrderItemDTO;
import com.example.webstore.dto.OrderRequest;
import com.example.webstore.model.Cart;
import com.example.webstore.model.CartItem;
import com.example.webstore.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CartService {

    @Inject
    CartItemService cartItemService;


    public CartService(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @Transactional
    public CartResponse getCartByUserId(Long userId) {
        if (userId == null) {
            throw new WebApplicationException("Missing userId", 400);
        }

        User user = User.findById(userId);
        if (user == null) {
            throw new WebApplicationException("User not found", 404);
        }

        Cart cart = Cart.find("user.id = ?1", userId).firstResult();
        if (cart == null) {
            cart = new Cart();
            cart.user = user;
            cart.items = new ArrayList<>();
            cart.persist();
        } else if (cart.items == null) {
            cart.items = new ArrayList<>();
        }

        return new CartResponse(cart);
    }


    @Transactional
    public CartResponse addToCart(Long userId, CartItemRequest request) {
        User user = User.findById(userId);
        if (user == null) {
            throw new WebApplicationException("User not found");
        }

        Cart cart = Cart.find("user.id = ?1", userId).firstResult();
        if (cart == null) {
            cart = new Cart();
            cart.user = user;
            cart.persist();
        }

        CartItem item = cartItemService.addItemToCart(cart, request);
        cart.items.add(item);

        return new CartResponse(cart);
    }


    @Transactional
    public void clearCart(Long userId) {
        Cart cart = Cart.find("user.id = ?1", userId).firstResult();
        if (cart != null && cart.items != null) {
            for (CartItem item : new ArrayList<>(cart.items)) {
                item.delete();
            }
            cart.items.clear();
        }
    }


    public OrderRequest generateOrderRequest(Long userId) {
        Cart cart = Cart.find("user.id = ?1", userId).firstResult();
        if (cart == null || cart.items == null || cart.items.isEmpty()) {
            return null;
        }

        OrderRequest request = new OrderRequest();
        request.userId = userId;
        request.totalPrice = cart.items.stream()
                .mapToDouble(item -> item.product.price * item.quantity)
                .sum();

        List<OrderItemDTO> items = cart.items.stream().map(item -> {
            OrderItemDTO dto = new OrderItemDTO();
            dto.productId = item.product.id;
            dto.quantity = item.quantity;
            return dto;
        }).toList();

        request.items = items;
        return request;
    }



    public int countItems(Long userId) {
        return CartItem.<CartItem>list("cart.user.id = ?1", userId)
                .stream()
                .mapToInt(ci -> ci.quantity)
                .sum();
    }




}