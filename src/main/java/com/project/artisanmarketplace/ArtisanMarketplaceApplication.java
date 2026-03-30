package com.project.artisanmarketplace;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.project.artisanmarketplace.model.Product;
import com.project.artisanmarketplace.repository.ProductRepository;

@SpringBootApplication
public class ArtisanMarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtisanMarketplaceApplication.class, args);
    }

    // <-- THIS IS WHERE YOU PUT IT
    @Bean
    CommandLineRunner runner(ProductRepository repository) {
        return args -> {
            repository.save(new Product("Handmade Pottery", "Traditional clay pottery", 500, "Ravi Kumar"));
            repository.save(new Product("Wooden Sculpture", "Hand carved wooden statue", 1200, "Lakshmi Devi"));
            repository.save(new Product("Handwoven Saree", "Pure cotton handloom saree", 2500, "Anita Sharma"));
        };
    }
}