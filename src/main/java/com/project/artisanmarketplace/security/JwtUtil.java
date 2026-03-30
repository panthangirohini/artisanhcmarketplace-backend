package com.project.artisanmarketplace.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Use a strong secret in production — move this to application.properties
    private static final String SECRET = "artisan-marketplace-super-secret-key-2024-do-not-expose";
    private static final long EXPIRY_MS = 86400000; // 24 hours

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String email, String role) {
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRY_MS))
            .signWith(key)
            .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        return (String) Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload().get("role");
    }

    public boolean isValid(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
