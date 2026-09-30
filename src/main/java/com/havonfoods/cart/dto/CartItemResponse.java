package com.havonfoods.cart.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
	private Long itemId;
    private Long variantId;
    private String productName;
    private String sku;
    private Integer weightInGrams;
    private String packagingType;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal lineTotal;
    private boolean available;
    private Integer availableStock;
}
