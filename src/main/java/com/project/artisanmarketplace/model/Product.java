package com.project.artisanmarketplace.model;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private double price;
    private String artisanName;
    private String imageUrl;

    // Category as a simple Long FK — keeps it lightweight
    private Long categoryId;

    public Product() {}

    public Product(String name, String description, double price, String artisanName) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.artisanName = artisanName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getArtisanName() { return artisanName; }
    public void setArtisanName(String artisanName) { this.artisanName = artisanName; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}