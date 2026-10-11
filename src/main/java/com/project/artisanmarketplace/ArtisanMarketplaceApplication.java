package com.project.artisanmarketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Sample data is created by config/DataSeeder (only when the database is empty)
@SpringBootApplication
public class ArtisanMarketplaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtisanMarketplaceApplication.class, args);
    }
}
