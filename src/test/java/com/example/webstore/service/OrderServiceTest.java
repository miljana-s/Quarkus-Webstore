package com.example.webstore.service;

import com.example.webstore.dto.OrderItemDTO;
import com.example.webstore.dto.OrderRequest;
import com.example.webstore.dto.OrderResponse;
import com.example.webstore.model.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@QuarkusTest
class OrderServiceTest {

    @Inject
    OrderService orderService;

    @Test
    void listAllOrders_initiallyEmptyOrNonNull() {
        var all = orderService.listAllOrders();
        assertThat(all).isNotNull();
    }

    @Test
    void createOrderFromRequest_happyPath_persistsWithItems_andUNCONFIRMEDDefault() {
        User customer = User.find("username", "user").firstResult();
        assertThat(customer).isNotNull();

        Product p = Product.find("name", "Engraved Bracelet").firstResult();
        if (p == null) {
            p = Product.findAll().firstResult();
        }
        assertThat(p).isNotNull();

        OrderItemDTO item = new OrderItemDTO(p.id, 2);
        OrderRequest req = new OrderRequest(customer.id, List.of(item), null, 240.0);

        Order created = orderService.createOrderFromRequest(req);
        assertThat(created).isNotNull();
        assertThat(created.id).isNotNull();
        assertThat(created.user.id).isEqualTo(customer.id);
        assertThat(created.items).hasSize(1);
        assertThat(created.items.get(0).product.id).isEqualTo(p.id);
        assertThat(created.status).isEqualTo(OrderStatus.UNCONFIRMED);
        assertThat(created.totalPrice).isEqualTo(240.0);

        List<OrderResponse> all = orderService.listAllOrders();
        assertThat(all.stream().anyMatch(or -> or.id.equals(created.id))).isTrue();
    }

    @Test
    void createOrderFromRequest_missingUser_throws400() {
        Product p = Product.findAll().firstResult();
        assertThat(p).isNotNull();

        OrderItemDTO item = new OrderItemDTO(p.id, 1);
        OrderRequest req = new OrderRequest(null, List.of(item), OrderStatus.UNCONFIRMED, 100.0);

        assertThatThrownBy(() -> orderService.createOrderFromRequest(req))
                .hasMessageContaining("User ID is required");
    }

    @Test
    void createOrderFromRequest_unknownProduct_throws404() {
        User customer = User.find("username", "user").firstResult();
        assertThat(customer).isNotNull();

        OrderItemDTO item = new OrderItemDTO(999999L, 1);
        OrderRequest req = new OrderRequest(customer.id, List.of(item), OrderStatus.UNCONFIRMED, 100.0);

        assertThatThrownBy(() -> orderService.createOrderFromRequest(req))
                .hasMessageContaining("does not exist");
    }

    @Test
    void updateStatus_changesStatus() {
        User customer = User.find("username", "user").firstResult();
        Product p = Product.findAll().firstResult();
        assertThat(customer).isNotNull();
        assertThat(p).isNotNull();

        OrderItemDTO item = new OrderItemDTO(p.id, 1);
        OrderRequest req = new OrderRequest(customer.id, List.of(item), OrderStatus.UNCONFIRMED, p.price);
        Order order = orderService.createOrderFromRequest(req);

        Order updated = orderService.updateStatus(order.id, OrderStatus.CONFIRMED);
        assertThat(updated.status).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void deleteOrder_removesEntity() {
        User customer = User.find("username", "user").firstResult();
        Product p = Product.findAll().firstResult();
        assertThat(customer).isNotNull();
        assertThat(p).isNotNull();

        OrderItemDTO item = new OrderItemDTO(p.id, 1);
        OrderRequest req = new OrderRequest(customer.id, List.of(item), OrderStatus.UNCONFIRMED, p.price);
        Order order = orderService.createOrderFromRequest(req);
        Long id = order.id;

        orderService.deleteOrder(id);
        Order deleted = Order.findById(id);
        assertThat(deleted).isNull();
    }
}
