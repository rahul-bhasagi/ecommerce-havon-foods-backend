package com.havonfoods.catalog.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.havonfoods.catalog.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
	
	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.variants WHERE p.slug = :slug AND p.active = true")
    Optional<Product> findBySlugWithVariants(@Param("slug") String slug);

    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);

    Page<Product> findByActiveTrue(Pageable pageable);
}
