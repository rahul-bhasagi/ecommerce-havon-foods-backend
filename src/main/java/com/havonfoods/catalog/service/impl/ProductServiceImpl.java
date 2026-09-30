package com.havonfoods.catalog.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.havonfoods.catalog.dto.AddBatchStockRequest;
import com.havonfoods.catalog.dto.BatchResponse;
import com.havonfoods.catalog.dto.CreateProductRequest;
import com.havonfoods.catalog.dto.CreateVariantRequest;
import com.havonfoods.catalog.dto.ProductResponse;
import com.havonfoods.catalog.dto.VariantResponse;
import com.havonfoods.catalog.entity.BatchInventory;
import com.havonfoods.catalog.entity.Category;
import com.havonfoods.catalog.entity.Product;
import com.havonfoods.catalog.entity.ProductVariant;
import com.havonfoods.catalog.repository.BatchInventoryRepository;
import com.havonfoods.catalog.repository.CategoryRepository;
import com.havonfoods.catalog.repository.ProductRepository;
import com.havonfoods.catalog.repository.ProductVariantRepository;
import com.havonfoods.catalog.service.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService{
	
	private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductVariantRepository variantRepository;
    private final BatchInventoryRepository batchRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugWithVariants(slug)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with slug: " + slug));
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        String slug = request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");

        Product product = Product.builder()
                .category(category)
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .ingredients(request.getIngredients())
                .spiceLevel(request.getSpiceLevel())
                .isVeg(request.isVeg())
                .active(true)
                .build();

        return mapToResponse(productRepository.save(product));
    }
    
    @Override
    @Transactional
    public VariantResponse addVariantToProduct(Long productId, CreateVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        if (variantRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + request.getSku());
        }

        if (request.getSellingPrice().compareTo(request.getMrp()) > 0) {
            throw new IllegalArgumentException("Selling price cannot exceed MRP");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(request.getSku().toUpperCase().trim())
                .weightInGrams(request.getWeightInGrams())
                .packagingType(request.getPackagingType())
                .mrp(request.getMrp())
                .sellingPrice(request.getSellingPrice())
                .active(true)
                .build();

        ProductVariant saved = variantRepository.save(variant);

        return VariantResponse.builder()
                .id(saved.getId())
                .sku(saved.getSku())
                .weightInGrams(saved.getWeightInGrams())
                .packagingType(saved.getPackagingType())
                .mrp(saved.getMrp())
                .sellingPrice(saved.getSellingPrice())
                .inStock(false)
                .build();
    }

    @Override
    @Transactional
    public BatchResponse addBatchStock(Long variantId, AddBatchStockRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found with ID: " + variantId));

        if (!request.getExpiryDate().isAfter(request.getManufacturingDate())) {
            throw new IllegalArgumentException("Expiry date must be after manufacturing date");
        }

        BatchInventory batch = BatchInventory.builder()
                .variant(variant)
                .batchNumber(request.getBatchNumber().trim())
                .manufacturingDate(request.getManufacturingDate())
                .expiryDate(request.getExpiryDate())
                .quantityAvailable(request.getQuantity())
                .quantityReserved(0)
                .build();

        BatchInventory saved = batchRepository.save(batch);

        return BatchResponse.builder()
                .id(saved.getId())
                .variantId(variant.getId())
                .batchNumber(saved.getBatchNumber())
                .manufacturingDate(saved.getManufacturingDate())
                .expiryDate(saved.getExpiryDate())
                .quantityAvailable(saved.getQuantityAvailable())
                .quantityReserved(saved.getQuantityReserved())
                .build();
    }
    
    
    

    private ProductResponse mapToResponse(Product product) {
        LocalDate today = LocalDate.now();

        List<VariantResponse> variantResponses = product.getVariants().stream()
                .filter(v -> v.isActive())
                .map(v -> {
                    int totalStock = v.getBatches().stream()
                            .filter(b -> b.getExpiryDate().isAfter(today))
                            .mapToInt(b -> b.getQuantityAvailable())
                            .sum();

                    return VariantResponse.builder()
                            .id(v.getId())
                            .sku(v.getSku())
                            .weightInGrams(v.getWeightInGrams())
                            .packagingType(v.getPackagingType())
                            .mrp(v.getMrp())
                            .sellingPrice(v.getSellingPrice())
                            .inStock(totalStock > 0)
                            .build();
                })
                .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .description(product.getDescription())
                .ingredients(product.getIngredients())
                .spiceLevel(product.getSpiceLevel())
                .isVeg(product.isVeg())
                .variants(variantResponses)
                .build();
    }
}
