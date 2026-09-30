package com.havonfoods.catalog.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
public class AddBatchStockRequest {
	
	@NotBlank(message = "Batch number is required")
    private String batchNumber; // e.g. "BATCH-2026-09-A"

    @NotNull(message = "Manufacturing date is required")
    @PastOrPresent(message = "Manufacturing date cannot be in the future")
    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1 unit")
    private Integer quantity;
}
