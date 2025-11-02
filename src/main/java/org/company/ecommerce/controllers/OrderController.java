package org.company.ecommerce.controllers;

import org.company.ecommerce.dto.*;
import org.company.ecommerce.models.Order;
import org.company.ecommerce.models.OrderItem;
import org.company.ecommerce.models.OrderStatus;
import org.company.ecommerce.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public List<OrderResponse> all() {
        return orderService.listAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return toResponse(orderService.getOrder(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        Order order = orderService.createOrder(req);
        return ResponseEntity.created(URI.create("/api/orders/" + order.getId())).body(toResponse(order));
    }

    @PutMapping("/{id}/status")
    public OrderResponse changeStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return toResponse(orderService.changeStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{email}")
    public List<OrderResponse> byCustomer(@PathVariable String email) {
        return orderService.findByCustomerEmail(email).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private OrderResponse toResponse(Order o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setCustomerName(o.getCustomerName());
        r.setCustomerEmail(o.getCustomerEmail());
        r.setOrderDate(o.getOrderDate());
        r.setStatus(o.getStatus().name());
        r.setTotalAmount(o.getTotalAmount());
        List<OrderItemResponse> items = o.getOrderItems().stream().map(this::toItemResponse).collect(Collectors.toList());
        r.setItems(items);
        return r;
    }

    private OrderItemResponse toItemResponse(OrderItem oi) {
        OrderItemResponse ir = new OrderItemResponse();
        ir.setId(oi.getId());
        ir.setProductId(oi.getProduct().getId());
        ir.setProductName(oi.getProduct().getName());
        ir.setQuantity(oi.getQuantity());
        ir.setUnitPrice(oi.getUnitPrice());
        ir.setTotalPrice(oi.getTotalPrice());
        return ir;
    }
}
