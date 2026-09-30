package com.havonfoods.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {
	@NotNull(message = "Delivery address ID is required")
    private Long addressId;
}
