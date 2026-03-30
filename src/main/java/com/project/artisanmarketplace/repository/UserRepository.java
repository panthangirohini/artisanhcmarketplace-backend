package com.project.artisanmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project.artisanmarketplace.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email — used for login and duplicate check
    User findByEmail(String email);

    // Check if an email is already registered (cleaner than findByEmail != null)
    boolean existsByEmail(String email);

    // Get all users by role — useful for admin dashboard later
    // Usage: userRepository.findByRole("ARTISAN")
    List<User> findByRole(String role);
}