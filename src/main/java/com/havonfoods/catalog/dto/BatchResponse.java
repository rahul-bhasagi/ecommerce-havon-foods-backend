package com.havonfoods.catalog.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BatchResponse {
	
	private Long id;
    private Long variantId;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private Integer quantityAvailable;
    private Integer quantityReserved;
}
