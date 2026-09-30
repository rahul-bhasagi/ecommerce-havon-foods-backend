package com.havonfoods.catalog.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String categoryName;
    private String description;
    private String ingredients;
    private String spiceLevel;
    private boolean isVeg;
    private List<VariantResponse> variants;
}