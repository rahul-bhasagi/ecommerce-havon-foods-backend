package com.havonfoods.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponse {
	private Long cartId;
    private String sessionToken;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal subTotal;
}
