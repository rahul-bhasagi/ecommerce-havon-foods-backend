package com.havonfoods.catalog.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.havonfoods.catalog.dto.AddBatchStockRequest;
import com.havonfoods.catalog.dto.BatchResponse;
import com.havonfoods.catalog.dto.CreateProductRequest;
import com.havonfoods.catalog.dto.CreateVariantRequest;
import com.havonfoods.catalog.dto.ProductResponse;
import com.havonfoods.catalog.dto.VariantResponse;

public interface ProductService {
	
	Page<ProductResponse> getAllActiveProducts(Pageable pageable);
    ProductResponse getProductBySlug(String slug);
    ProductResponse createProduct(CreateProductRequest request);
    
    VariantResponse addVariantToProduct(Long productId, CreateVariantRequest request);
    BatchResponse addBatchStock(Long variantId, AddBatchStockRequest request);
}
