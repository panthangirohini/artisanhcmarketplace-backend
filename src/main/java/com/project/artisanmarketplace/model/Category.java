// ---- Category.java ----
package com.project.artisanmarketplace.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;   // e.g. "Pottery", "Textiles", "Jewellery"

    private String description;

    // Getters and Setters
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}


// ---- Add this to Product.java (inside the class, alongside other fields) ----

// Add this import to Product.java:
// import jakarta.persistence.ManyToOne;
// import jakarta.persistence.JoinColumn;

// Add this field:
// @ManyToOne
// @JoinColumn(name = "category_id")
// private Category category;

// Add getter/setter:
// public Category getCategory() { return category; }
// public void setCategory(Category category) { this.category = category; }
