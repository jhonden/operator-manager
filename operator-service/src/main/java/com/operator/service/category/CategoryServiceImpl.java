package com.operator.service.category;

import com.operator.common.dto.category.CategoryRequest;
import com.operator.common.dto.operator.OperatorResponse;
import com.operator.common.dto.pkg.CategoryResponse;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.core.operator.domain.Category;
import com.operator.core.operator.domain.Operator;
import com.operator.core.operator.repository.CategoryRepository;
import com.operator.core.operator.repository.OperatorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Category Service Implementation (Stub)
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final OperatorRepository operatorRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating category: {}", request.getName());

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());

        if (request.getParentId() != null) {
            Category parent = new Category();
            parent.setId(request.getParentId());
            category.setParent(parent);
        }

        category.setOrderIndex(request.getOrderIndex());
        category.setOperatorCount(0);

        category = categoryRepository.save(category);

        return mapToResponse(category);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setOrderIndex(request.getOrderIndex());

        category = categoryRepository.save(category);

        return mapToResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        // Check if has children
        if (!category.getChildren().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category with children. Delete or move children first.");
        }

        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryResponse> getCategoryTree() {
        List<Category> categories = categoryRepository.findCategoryTree();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        List<Category> categories = categoryRepository.findByParentIsNullOrderByOrderIndexAsc();
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getChildCategories(Long parentId) {
        List<Category> categories = categoryRepository.findByParentIdOrderByOrderIndexAsc(parentId);
        return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OperatorResponse> getOperatorsByCategory(Long categoryId) {
        List<Operator> operators = operatorRepository.findByCategoryId(categoryId);
        return operators.stream()
                .map(this::mapOperatorToResponse)
                .collect(Collectors.toList());
    }

    private OperatorResponse mapOperatorToResponse(Operator operator) {
        return OperatorResponse.builder()
                .id(operator.getId())
                .name(operator.getName())
                .description(operator.getDescription())
                .language(operator.getLanguage())
                .status(operator.getStatus())
                .version(operator.getVersion())
                .build();
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setParentId(category.getParent() != null ? category.getParent().getId() : null);
        response.setDescription(category.getDescription());
        return response;
    }
}
