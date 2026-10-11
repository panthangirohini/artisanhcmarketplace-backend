package com.project.artisanmarketplace.config;

import com.project.artisanmarketplace.model.Category;
import com.project.artisanmarketplace.model.Product;
import com.project.artisanmarketplace.model.User;
import com.project.artisanmarketplace.repository.CategoryRepository;
import com.project.artisanmarketplace.repository.ProductRepository;
import com.project.artisanmarketplace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

// Fills an empty database with categories, a few sample products and an admin account,
// so the frontend has something to show on first run. Disable with SEED_DATA=false.
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    public DataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository,
                      UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                new Category("Pottery", "Hand-thrown clay and terracotta", "🏺"),
                new Category("Textiles", "Handloom weaves and embroidery", "🧵"),
                new Category("Jewellery", "Handcrafted ornaments", "💍"),
                new Category("Woodcraft", "Carved and turned wood", "🪵"),
                new Category("Paintings", "Folk and traditional art", "🎨"),
                new Category("Home Decor", "Lamps, baskets and more", "🪔")
            ));
        }

        if (productRepository.count() == 0) {
            Long pottery   = categoryId("Pottery");
            Long textiles  = categoryId("Textiles");
            Long jewellery = categoryId("Jewellery");
            Long wood      = categoryId("Woodcraft");
            Long painting  = categoryId("Paintings");
            Long decor     = categoryId("Home Decor");

            productRepository.saveAll(List.of(
                product("Khurja Blue Pottery Vase", "Hand-painted ceramic vase in the classic cobalt-blue Khurja style.", 1299, "Meera Ceramics", pottery),
                product("Terracotta Tea Set", "Six kulhad cups and a teapot, wheel-thrown and sun-baked.", 899, "Mitti Studio", pottery),
                product("Banarasi Silk Stole", "Pure silk stole with zari motifs, woven on a traditional handloom.", 2499, "Varanasi Weavers", textiles),
                product("Kantha Embroidered Throw", "Layered cotton throw with running-stitch Kantha work from Bengal.", 1899, "Shantiniketan Crafts", textiles),
                product("Oxidised Silver Jhumkas", "Lightweight temple-style jhumkas with oxidised finish.", 649, "Rajasthan Silverworks", jewellery),
                product("Channapatna Wooden Toys", "Set of lacquered wooden toys coloured with natural dyes.", 749, "Channapatna Collective", wood),
                product("Madhubani Painting", "Hand-painted Madhubani artwork on handmade paper, 12×16 in.", 1599, "Mithila Art House", painting),
                product("Brass Diya Set", "Set of four hand-cast brass diyas with engraved patterns.", 999, "Moradabad Metalcraft", decor)
            ));
        }

        if (userRepository.findByRole("ADMIN").isEmpty() && !userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }

    private Long categoryId(String name) {
        Category c = categoryRepository.findByName(name);
        return c == null ? null : c.getId();
    }

    private Product product(String name, String description, double price, String artisan, Long categoryId) {
        Product p = new Product(name, description, price, artisan);
        p.setCategoryId(categoryId);
        return p;
    }
}
