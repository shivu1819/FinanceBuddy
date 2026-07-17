package com.financebuddy.backend.service.impl;

import com.financebuddy.backend.dto.CategoryRequest;
import com.financebuddy.backend.dto.CategoryResponse;
import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getCurrentUser();

        categoryRepository.findByUserIdAndName(user.getId(), request.getName())
                .ifPresent(category -> {
                    throw new IllegalArgumentException("Category already exists");
                });

        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .color(request.getColor())
                .icon(request.getIcon())
                .systemDefault(false)
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        User user = getCurrentUser();
        return categoryRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        return mapToResponse(getOwnedCategory(id));
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = getOwnedCategory(id);

        categoryRepository.findByUserIdAndName(category.getUser().getId(), request.getName())
                .filter(existingCategory -> !existingCategory.getId().equals(id))
                .ifPresent(existingCategory -> {
                    throw new IllegalArgumentException("Category already exists");
                });

        category.setName(request.getName());
        category.setType(request.getType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getOwnedCategory(id);
        categoryRepository.delete(category);
    }

    private Category getOwnedCategory(Long id) {
        User user = getCurrentUser();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Category not found");
        }

        return category;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .color(category.getColor())
                .icon(category.getIcon())
                .build();
    }
}
