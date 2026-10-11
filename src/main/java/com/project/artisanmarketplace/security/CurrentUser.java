package com.project.artisanmarketplace.security;

import com.project.artisanmarketplace.model.User;
import com.project.artisanmarketplace.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

// Resolves the logged-in user from the JWT-backed Authentication (its name is the user's email)
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get(Authentication auth) {
        return auth == null ? null : userRepository.findByEmail(auth.getName());
    }

    public static boolean isAdmin(Authentication auth) {
        return hasRole(auth, "ADMIN");
    }

    public static boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
