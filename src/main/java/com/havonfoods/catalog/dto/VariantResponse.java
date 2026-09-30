package com.havonfoods.catalog.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VariantResponse {
    
	private Long id;
    private String sku;
    private Integer weightInGrams;
    private String packagingType;
    private BigDecimal mrp;
    private BigDecimal sellingPrice;
    private boolean inStock;
}