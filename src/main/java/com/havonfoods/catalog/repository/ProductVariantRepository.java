package com.havonfoods.catalog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.havonfoods.catalog.entity.ProductVariant;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long>{
	
	Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ProductVariant> findByProductIdAndActiveTrue(Long productId);

    // Eagerly fetches batches to avoid N+1 query problems when calculating stock
    @Query("SELECT pv FROM ProductVariant pv LEFT JOIN FETCH pv.batches WHERE pv.id = :id AND pv.active = true")
    Optional<ProductVariant> findByIdWithBatches(@Param("id") Long id);
}
