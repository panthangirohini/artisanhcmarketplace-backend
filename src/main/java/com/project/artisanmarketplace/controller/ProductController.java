package com.project.artisanmarketplace.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project.artisanmarketplace.model.Product;
import com.project.artisanmarketplace.repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Advanced filter endpoint: GET /api/products/filter?keyword=silk&categoryId=2&minPrice=500&maxPrice=5000
    @GetMapping("/filter")
    public List<Product> filterProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long   categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        return productRepository.findWithFilters(keyword, categoryId, minPrice, maxPrice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return productRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product updated) {
        return productRepository.findById(id).map(p -> {
            p.setName(updated.getName());
            p.setDescription(updated.getDescription());
            p.setPrice(updated.getPrice());
            p.setArtisanName(updated.getArtisanName());
            p.setCategoryId(updated.getCategoryId());
            if (updated.getImageUrl() != null && !updated.getImageUrl().isBlank())
                p.setImageUrl(updated.getImageUrl());
            return ResponseEntity.ok(productRepository.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) return ResponseEntity.notFound().build();
        productRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("No file provided");
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/"))
                return ResponseEntity.badRequest().body("Only images allowed");

            String uploadDir = "uploads/";
            new File(uploadDir).mkdirs();

            String ext = file.getOriginalFilename() != null && file.getOriginalFilename().contains(".")
                ? file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID() + ext;

            Files.copy(file.getInputStream(), Paths.get(uploadDir + fileName), StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(fileName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}