package com.operator.service.category;

import com.operator.common.dto.category.CategoryRequest;
import com.operator.common.dto.operator.OperatorResponse;
import com.operator.common.dto.pkg.CategoryResponse;

import java.util.List;

/**
 * Category Service Interface
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface CategoryService {

    /**
     * Create a new category
     */
    CategoryResponse createCategory(CategoryRequest request);

    /**
     * Update category
     */
    CategoryResponse updateCategory(Long id, CategoryRequest request);

    /**
     * Delete category
     */
    void deleteCategory(Long id);

    /**
     * Get category by ID
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * Get all categories (tree structure)
     */
    List<CategoryResponse> getCategoryTree();

    /**
     * Get root categories
     */
    List<CategoryResponse> getRootCategories();

    /**
     * Get child categories
     */
    List<CategoryResponse> getChildCategories(Long parentId);

    /**
     * Get operators in category
     */
    List<OperatorResponse> getOperatorsByCategory(Long categoryId);
}
