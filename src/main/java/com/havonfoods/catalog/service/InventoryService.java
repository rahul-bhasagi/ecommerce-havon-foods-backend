package com.havonfoods.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.havonfoods.catalog.entity.BatchInventory;
import com.havonfoods.catalog.repository.BatchInventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
	private final BatchInventoryRepository batchRepo;

    /**
     * Deducts stock using FEFO (First Expired, First Out)
     */
    @Transactional
    public void deductStockFefo(Long variantId, int quantityToDeduct) {
        // Fetch batches ordered by expiryDate ASC
        List<BatchInventory> batches = batchRepo.findByVariantIdAndQuantityAvailableGreaterThanOrderByExpiryDateAsc(
                variantId, 0
        );

        int remaining = quantityToDeduct;

        for (BatchInventory batch : batches) {
            if (remaining <= 0) break;

            int available = batch.getQuantityAvailable();
            if (available >= remaining) {
                batch.setQuantityAvailable(available - remaining);
                remaining = 0;
            } else {
                remaining -= available;
                batch.setQuantityAvailable(0);
            }
            batchRepo.save(batch);
        }

        if (remaining > 0) {
            throw new IllegalStateException("Insufficient batch inventory for variant ID: " + variantId);
        }
    }
}
