package com.project.artisanmarketplace.controller;

import com.project.artisanmarketplace.model.Order;
import com.project.artisanmarketplace.model.OrderItem;
import com.project.artisanmarketplace.model.Product;
import com.project.artisanmarketplace.model.User;
import com.project.artisanmarketplace.repository.OrderRepository;
import com.project.artisanmarketplace.repository.ProductRepository;
import com.project.artisanmarketplace.security.CurrentUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    // Same rule as the checkout page: free shipping above ₹999, otherwise ₹79
    private static final double FREE_SHIPPING_OVER = 999;
    private static final double SHIPPING_FEE = 79;

    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CurrentUser currentUser;

    // Orders that contain the logged-in artisan's products.
    // Items and totalAmount are limited to that artisan's own products; orderTotal is the full order value.
    @GetMapping("/artisan")
    public List<Map<String, Object>> getArtisanOrders(Authentication auth) {
        User artisan = currentUser.get(auth);
        if (artisan == null || artisan.getName() == null) return List.of();

        Set<Long> productIds = productIdsOf(artisan.getName());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Order order : orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))) {
            if (order.getItems() == null) continue;
            List<OrderItem> mine = order.getItems().stream()
                .filter(i -> productIds.contains(i.getProductId()))
                .toList();
            if (mine.isEmpty()) continue;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", order.getId());
            row.put("userId", order.getUserId());
            row.put("status", order.getStatus());
            row.put("createdAt", order.getCreatedAt());
            row.put("shippingAddress", order.getShippingAddress());
            row.put("paymentId", order.getPaymentId());
            row.put("items", mine);
            row.put("totalAmount", mine.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum());
            row.put("orderTotal", order.getTotalAmount());
            result.add(row);
        }
        return result;
    }

    // Place a new order
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Order order, Authentication auth) {
        User buyer = currentUser.get(auth);
        if (buyer == null) return ResponseEntity.status(401).build();
        if (order.getItems() == null || order.getItems().isEmpty())
            return ResponseEntity.badRequest().body("Your order has no items");

        // Never trust prices from the browser — use the catalogue's current name and price
        double subtotal = 0;
        for (OrderItem item : order.getItems()) {
            if (item.getQuantity() < 1) return ResponseEntity.badRequest().body("Invalid quantity");
            Product product = item.getProductId() == null ? null
                : productRepository.findById(item.getProductId()).orElse(null);
            if (product == null)
                return ResponseEntity.badRequest().body("A product in your bag is no longer available");
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setOrder(order);
            subtotal += product.getPrice() * item.getQuantity();
        }

        order.setUserId(buyer.getId());   // the buyer comes from the token, not the request body
        order.setTotalAmount(subtotal + (subtotal > FREE_SHIPPING_OVER ? 0 : SHIPPING_FEE));
        order.setPaymentId(null);
        order.setStatus(Order.OrderStatus.PENDING);
        return ResponseEntity.ok(orderRepository.save(order));
    }

    // Orders of one user — only that user or an admin may look
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId, Authentication auth) {
        User me = currentUser.get(auth);
        if (me == null) return ResponseEntity.status(401).build();
        if (!CurrentUser.isAdmin(auth) && !me.getId().equals(userId))
            return ResponseEntity.status(403).body("You can only view your own orders");
        return ResponseEntity.ok(orderRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    // Single order — its buyer or an admin
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id, Authentication auth) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        if (!isBuyerOrAdmin(order, auth)) return ResponseEntity.status(403).body("Not your order");
        return ResponseEntity.ok(order);
    }

    // Update status — admins any order, artisans only orders that contain their products
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status, Authentication auth) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();

        if (!CurrentUser.isAdmin(auth)) {
            User artisan = currentUser.get(auth);
            Set<Long> mine = artisan == null || artisan.getName() == null ? Set.of()
                : productIdsOf(artisan.getName());
            boolean sellsInThisOrder = order.getItems() != null
                && order.getItems().stream().anyMatch(i -> mine.contains(i.getProductId()));
            if (!sellsInThisOrder) return ResponseEntity.status(403).body("This order has none of your products");
        }

        try {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Unknown status: " + status);
        }
        return ResponseEntity.ok(orderRepository.save(order));
    }

    // Confirm payment (demo checkout) — only the buyer, and only while the order is pending
    @PostMapping("/{id}/confirm-payment")
    public ResponseEntity<?> confirmPayment(@PathVariable Long id, @RequestParam String paymentId, Authentication auth) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) return ResponseEntity.notFound().build();
        if (!isBuyerOrAdmin(order, auth)) return ResponseEntity.status(403).body("Not your order");
        if (order.getStatus() != Order.OrderStatus.PENDING)
            return ResponseEntity.badRequest().body("This order is already " + order.getStatus().name().toLowerCase());

        order.setPaymentId(paymentId);
        order.setStatus(Order.OrderStatus.CONFIRMED);
        return ResponseEntity.ok(orderRepository.save(order));
    }

    private boolean isBuyerOrAdmin(Order order, Authentication auth) {
        if (CurrentUser.isAdmin(auth)) return true;
        User me = currentUser.get(auth);
        return me != null && me.getId().equals(order.getUserId());
    }

    private Set<Long> productIdsOf(String artisanName) {
        return productRepository.findByArtisanNameIgnoreCase(artisanName).stream()
            .map(Product::getId)
            .collect(Collectors.toSet());
    }
}
