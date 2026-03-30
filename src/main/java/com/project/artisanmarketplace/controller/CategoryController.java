package com.project.artisanmarketplace.controller;
 
import com.project.artisanmarketplace.model.Category;
import com.project.artisanmarketplace.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
 
    @Autowired
    private CategoryRepository categoryRepository;
 
    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }
 
    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        if (categoryRepository.existsByName(category.getName()))
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(categoryRepository.save(category));
    }
 
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok("Deleted");
    }
}
