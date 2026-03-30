package com.project.artisanmarketplace.controller;

import com.project.artisanmarketplace.model.Order;
import com.project.artisanmarketplace.model.OrderItem;
import com.project.artisanmarketplace.repository.OrderRepository;
import com.project.artisanmarketplace.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private JwtUtil jwtUtil;

    // Place a new order
    @PostMapping
    public ResponseEntity<Order> placeOrder(
            @RequestBody Order order,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        // You could also look up the user ID from email here
        String email = jwtUtil.extractEmail(token);

        // Link each item back to the order
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
            }
        }

        order.setStatus(Order.OrderStatus.PENDING);
        Order saved = orderRepository.save(order);
        return ResponseEntity.ok(saved);
    }

    // Get all orders for a specific user
    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Get single order details
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // Update order status (artisan/admin only — protect with Spring Security in prod)
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return orderRepository.findById(id).map(order -> {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }

    // Confirm payment — called after Razorpay webhook or frontend verification
    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<Order> confirmPayment(
            @PathVariable Long id,
            @RequestParam String paymentId) {

        return orderRepository.findById(id).map(order -> {
            order.setPaymentId(paymentId);
            order.setStatus(Order.OrderStatus.CONFIRMED);
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }
}
