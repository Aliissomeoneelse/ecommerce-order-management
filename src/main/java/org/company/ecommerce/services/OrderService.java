package org.company.ecommerce.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.company.ecommerce.dto.CreateOrderRequest;
import org.company.ecommerce.exceptions.*;
import org.company.ecommerce.models.*;
import org.company.ecommerce.repository.OrderRepository;
import org.company.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        log.info("Creating order for customer: {}", req.getCustomerEmail());

        if (!StringUtils.hasText(req.getCustomerName()) || !StringUtils.hasText(req.getCustomerEmail())) {
            log.error("Invalid customer info: name={}, email={}", req.getCustomerName(), req.getCustomerEmail());
            throw new IllegalArgumentException("Customer name and email required");
        }

        if (req.getItems() == null || req.getItems().isEmpty()) {
            log.warn("Attempt to create empty order for {}", req.getCustomerEmail());
            throw new IllegalArgumentException("Order must have items");
        }

        Order order = new Order();
        order.setCustomerName(req.getCustomerName());
        order.setCustomerEmail(req.getCustomerEmail());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itReq : req.getItems()) {
            Product product = productRepository.findById(itReq.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itReq.getProductId()));

            log.debug("Adding product {} (qty {}) to order", product.getName(), itReq.getQuantity());

            if (product.getStock() == null || product.getStock() <= 0) {
                log.error("Insufficient stock for product {}", product.getId());
                throw new InsufficientStockException(product.getId());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itReq.getQuantity()));

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(product);
            oi.setQuantity(itReq.getQuantity());
            oi.setUnitPrice(unitPrice);
            oi.setTotalPrice(itemTotal);

            items.add(oi);
            total = total.add(itemTotal);
        }

        order.setOrderItems(items);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        log.info("Order {} created successfully with total {}", saved.getId(), saved.getTotalAmount());
        return saved;
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    public List<Order> findByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmail(email);
    }

    @Transactional
    public Order changeStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrder(orderId);
        OrderStatus old = order.getStatus();

        if (old == newStatus) return order;
        if (old == OrderStatus.CANCELLED || old == OrderStatus.DELIVERED) {
            throw new InvalidOrderStatusException("Cannot change status from " + old);
        }

        if (old == OrderStatus.PENDING && newStatus == OrderStatus.CONFIRMED) {
            for (OrderItem oi : order.getOrderItems()) {
                Product p = productRepository.findById(oi.getProduct().getId())
                        .orElseThrow(() -> new ProductNotFoundException(oi.getProduct().getId()));
                if (p.getStock() < oi.getQuantity()) {
                    throw new InsufficientStockException(p.getId());
                }
            }
            for (OrderItem oi : order.getOrderItems()) {
                Product p = productRepository.findById(oi.getProduct().getId()).get();
                p.setStock(p.getStock() - oi.getQuantity());
                productRepository.save(p);
            }
            order.setStatus(OrderStatus.CONFIRMED);
            return orderRepository.save(order);
        }

        if (old == OrderStatus.CONFIRMED && newStatus == OrderStatus.CANCELLED) {
            for (OrderItem oi : order.getOrderItems()) {
                Product p = productRepository.findById(oi.getProduct().getId()).orElseThrow(() -> new ProductNotFoundException(oi.getProduct().getId()));
                p.setStock(p.getStock() + oi.getQuantity());
                productRepository.save(p);
            }
            order.setStatus(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        }

        if (old == OrderStatus.CONFIRMED && newStatus == OrderStatus.SHIPPED) {
            order.setStatus(OrderStatus.SHIPPED);
            return orderRepository.save(order);
        }
        if (old == OrderStatus.SHIPPED && newStatus == OrderStatus.DELIVERED) {
            order.setStatus(OrderStatus.DELIVERED);
            return orderRepository.save(order);
        }
        if (old == OrderStatus.PENDING && newStatus == OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            return orderRepository.save(order);
        }

        throw new InvalidOrderStatusException("Unsupported status transition from " + old + " to " + newStatus);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = getOrder(id);
        if (order.getStatus() == OrderStatus.CANCELLED) return;
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            for (OrderItem oi : order.getOrderItems()) {
                Product p = productRepository.findById(oi.getProduct().getId())
                        .orElseThrow(() -> new ProductNotFoundException(oi.getProduct().getId()));
                p.setStock(p.getStock() + oi.getQuantity());
                productRepository.save(p);
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
