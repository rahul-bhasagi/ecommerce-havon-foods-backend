package com.havonfoods.catalog.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateVariantRequest {
	
	@NotBlank(message = "SKU cannot be empty")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "SKU must contain only uppercase letters, numbers, hyphens, and underscores")
    private String sku;

    @NotNull(message = "Weight in grams is required")
    @Min(value = 1, message = "Weight must be at least 1 gram")
    private Integer weightInGrams;

    @NotBlank(message = "Packaging type is required")
    private String packagingType; // e.g. "Glass Jar", "Food-Grade Pouch"

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "MRP must be greater than zero")
    private BigDecimal mrp;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Selling price must be greater than zero")
    private BigDecimal sellingPrice;
}
