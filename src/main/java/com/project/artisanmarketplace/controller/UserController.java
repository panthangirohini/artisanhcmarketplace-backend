package com.project.artisanmarketplace.controller;

import com.project.artisanmarketplace.model.User;
import com.project.artisanmarketplace.repository.UserRepository;
//import com.project.artisanmarketplace.security.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.project.artisanmarketplace.security.JwtUtil;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank())
            return ResponseEntity.badRequest().body("Email is required");
        if (user.getPassword() == null || user.getPassword().isBlank())
            return ResponseEntity.badRequest().body("Password is required");
        if (userRepository.existsByEmail(user.getEmail()))
            return ResponseEntity.badRequest().body("Email already registered");

        if (user.getRole() == null || user.getRole().isBlank())
            user.setRole("CUSTOMER");

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User saved = userRepository.save(user);
        saved.setPassword(null);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        if (user.getEmail() == null || user.getPassword() == null)
            return ResponseEntity.badRequest().body("Email and password required");

        User existing = userRepository.findByEmail(user.getEmail());
        if (existing == null || !passwordEncoder.matches(user.getPassword(), existing.getPassword()))
            return ResponseEntity.status(401).body("Invalid email or password");

        String token = jwtUtil.generateToken(existing.getEmail(), existing.getRole());

        return ResponseEntity.ok(Map.of(
            "token", token,
            "id",    existing.getId(),
            "name",  existing.getName(),
            "email", existing.getEmail(),
            "role",  existing.getRole()
        ));
    }
}