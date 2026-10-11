package com.project.artisanmarketplace.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
import com.project.artisanmarketplace.model.User;
import com.project.artisanmarketplace.repository.ProductRepository;
import com.project.artisanmarketplace.security.CurrentUser;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CurrentUser currentUser;

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

    // Artisans always list under their own name; admins may list on behalf of any artisan
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody Product product, Authentication auth) {
        User me = currentUser.get(auth);
        if (me == null) return ResponseEntity.status(401).build();
        if (!CurrentUser.isAdmin(auth) || product.getArtisanName() == null || product.getArtisanName().isBlank())
            product.setArtisanName(me.getName());
        product.setId(null);
        return ResponseEntity.ok(productRepository.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product updated, Authentication auth) {
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        if (!canManage(p, auth)) return ResponseEntity.status(403).body("You can only edit your own products");

        p.setName(updated.getName());
        p.setDescription(updated.getDescription());
        p.setPrice(updated.getPrice());
        p.setCategoryId(updated.getCategoryId());
        if (CurrentUser.isAdmin(auth) && updated.getArtisanName() != null && !updated.getArtisanName().isBlank())
            p.setArtisanName(updated.getArtisanName());
        if (updated.getImageUrl() != null && !updated.getImageUrl().isBlank())
            p.setImageUrl(updated.getImageUrl());
        return ResponseEntity.ok(productRepository.save(p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id, Authentication auth) {
        Product p = productRepository.findById(id).orElse(null);
        if (p == null) return ResponseEntity.notFound().build();
        if (!canManage(p, auth)) return ResponseEntity.status(403).body("You can only delete your own products");
        productRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }

    // Admins manage everything; artisans only products listed under their own name
    private boolean canManage(Product p, Authentication auth) {
        if (CurrentUser.isAdmin(auth)) return true;
        User me = currentUser.get(auth);
        return me != null && me.getName() != null && me.getName().equalsIgnoreCase(p.getArtisanName());
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) return ResponseEntity.badRequest().body("No file provided");
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/"))
                return ResponseEntity.badRequest().body("Only images allowed");

            // The extension decides how the file is served later, so only allow real image types
            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String ext = original.contains(".") ? original.substring(original.lastIndexOf(".")).toLowerCase() : ".jpg";
            if (!IMAGE_EXTENSIONS.contains(ext))
                return ResponseEntity.badRequest().body("Use a JPG, PNG, GIF or WebP image");

            String uploadDir = "uploads/";
            new File(uploadDir).mkdirs();
            String fileName = UUID.randomUUID() + ext;

            Files.copy(file.getInputStream(), Paths.get(uploadDir + fileName), StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(fileName);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}