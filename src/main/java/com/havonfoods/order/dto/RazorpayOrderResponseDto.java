package com.havonfoods.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RazorpayOrderResponseDto {
	private String razorpayOrderId;
    private BigDecimal amount;
    private String currency;
    private String keyId;
    private Long internalOrderId;
}
