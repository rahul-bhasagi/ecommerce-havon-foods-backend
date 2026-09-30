package com.havonfoods.catalog.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.havonfoods.catalog.entity.BatchInventory;

@Repository
public interface BatchInventoryRepository extends JpaRepository<BatchInventory, Long>{
	
	// First Expired, First Out (FEFO) query for active stock allocation
    @Query("SELECT b FROM BatchInventory b WHERE b.variant.id = :variantId " +
           "AND b.quantityAvailable > 0 AND b.expiryDate > :currentDate " +
           "ORDER BY b.expiryDate ASC")
    List<BatchInventory> findAvailableBatchesByFefo(
            @Param("variantId") Long variantId,
            @Param("currentDate") LocalDate currentDate
    );
}
