package com.havonfoods.catalog.service;

import java.util.List;

import com.havonfoods.catalog.dto.CategoryRequest;
import com.havonfoods.catalog.dto.CategoryResponse;

public interface CategoryService {
	List<CategoryResponse> getAllActiveCategories();
    CategoryResponse getCategoryBySlug(String slug);
    CategoryResponse createCategory(CategoryRequest request);
}
