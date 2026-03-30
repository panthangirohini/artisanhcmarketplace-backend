package com.project.artisanmarketplace.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.artisanmarketplace.model.Order;
import com.project.artisanmarketplace.model.Product;
import com.project.artisanmarketplace.model.User;
import com.project.artisanmarketplace.repository.OrderRepository;
import com.project.artisanmarketplace.repository.ProductRepository;
import com.project.artisanmarketplace.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired private UserRepository    userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository   orderRepository;

    // ── Users ──────────────────────────────────────

    @GetMapping("/users")
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPassword(null)); // never expose passwords
        return users;
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id))
            return ResponseEntity.notFound().build();
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        return userRepository.findById(id).map(user -> {
            user.setRole(role.toUpperCase());
            user.setPassword(null);
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Products ────────────────────────────────────

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id))
            return ResponseEntity.notFound().build();
        productRepository.deleteById(id);
        return ResponseEntity.ok("Product deleted");
    }

    // ── Orders ──────────────────────────────────────

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Dashboard stats ─────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        long totalUsers    = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders   = orderRepository.count();
        long artisans      = userRepository.findByRole("ARTISAN").size();
        double totalRevenue = orderRepository.findAll().stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CONFIRMED
                      || o.getStatus() == Order.OrderStatus.DELIVERED
                      || o.getStatus() == Order.OrderStatus.SHIPPED)
            .mapToDouble(Order::getTotalAmount)
            .sum();

        return ResponseEntity.ok(Map.of(
            "totalUsers",    totalUsers,
            "totalProducts", totalProducts,
            "totalOrders",   totalOrders,
            "totalArtisans", artisans,
            "totalRevenue",  totalRevenue
        ));
    }
}
