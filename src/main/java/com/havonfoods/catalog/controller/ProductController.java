package com.havonfoods.catalog.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.havonfoods.catalog.dto.AddBatchStockRequest;
import com.havonfoods.catalog.dto.BatchResponse;
import com.havonfoods.catalog.dto.CreateProductRequest;
import com.havonfoods.catalog.dto.CreateVariantRequest;
import com.havonfoods.catalog.dto.ProductResponse;
import com.havonfoods.catalog.dto.VariantResponse;
import com.havonfoods.catalog.service.ProductService;
import com.havonfoods.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
	
	private final ProductService productService;

    // Public browsing endpoint for storefront
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllActiveProducts(pageable), "Products retrieved"));
    }

    // Public detail endpoint
    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug), "Product retrieved"));
    }

    // Admin-only creation endpoint
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product created successfully"));
    }
    
    @PostMapping("/{productId}/variants")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<VariantResponse>> addVariant(
            @PathVariable Long productId,
            @Valid @RequestBody CreateVariantRequest request
    ) {
        VariantResponse response = productService.addVariantToProduct(productId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Product variant created successfully"));
    }

    // Record incoming physical batch stock for a specific SKU variant
    @PostMapping("/variants/{variantId}/batches")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<BatchResponse>> addBatchStock(
            @PathVariable Long variantId,
            @Valid @RequestBody AddBatchStockRequest request
    ) {
        BatchResponse response = productService.addBatchStock(variantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Batch inventory logged successfully"));
    }
    
    
    
}
