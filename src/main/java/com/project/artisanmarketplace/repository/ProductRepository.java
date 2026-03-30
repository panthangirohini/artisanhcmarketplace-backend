package com.project.artisanmarketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.artisanmarketplace.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByArtisanNameIgnoreCase(String artisanName);
    List<Product> findByPriceBetween(double min, double max);
    List<Product> findByCategoryId(Long categoryId);
    boolean existsByNameIgnoreCase(String name);

    // Combined filter: keyword + category + price range
    @Query("SELECT p FROM Product p WHERE " +"(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +"(:categoryId IS NULL OR p.categoryId = :categoryId) AND " +"(:minPrice IS NULL OR p.price >= :minPrice) AND " +"(:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> findWithFilters(
        @Param("keyword")    String keyword,
        @Param("categoryId") Long   categoryId,
        @Param("minPrice")   Double minPrice,
        @Param("maxPrice")   Double maxPrice
    );
}