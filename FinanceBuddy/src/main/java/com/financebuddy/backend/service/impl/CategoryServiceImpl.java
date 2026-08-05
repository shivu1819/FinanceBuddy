package com.financebuddy.backend.service.impl;

import com.financebuddy.backend.dto.CategoryRequest;
import com.financebuddy.backend.dto.CategoryResponse;
import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.MonthlyBudgetRepository;
import com.financebuddy.backend.repository.RecurringTransactionRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getCurrentUser();
        String displayName = request.getName().trim();
        String normalizedName = normalizeName(displayName);

        if (categoryRepository.existsByUserIdAndNormalizedName(user.getId(), normalizedName)) {
            throw categoryConflict();
        }

        Category category = Category.builder()
                .user(user)
                .name(displayName)
                .normalizedName(normalizedName)
                .type(request.getType())
                .color(request.getColor())
                .icon(request.getIcon())
                .systemDefault(false)
                .build();

        try {
            return mapToResponse(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException exception) {
            throw categoryConflict();
        }
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
        String displayName = request.getName().trim();
        String normalizedName = normalizeName(displayName);

        if (categoryRepository.existsByUserIdAndNormalizedNameAndIdNot(
                category.getUser().getId(),
                normalizedName,
                id
        )) {
            throw categoryConflict();
        }

        category.setName(displayName);
        category.setNormalizedName(normalizedName);
        category.setType(request.getType());
        category.setColor(request.getColor());
        category.setIcon(request.getIcon());

        try {
            return mapToResponse(categoryRepository.saveAndFlush(category));
        } catch (DataIntegrityViolationException exception) {
            throw categoryConflict();
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getOwnedCategory(id);
        Long userId = category.getUser().getId();

        if (transactionRepository.existsByCategoryIdAndUserId(id, userId)
                || monthlyBudgetRepository.existsByCategoryIdAndUserId(id, userId)
                || recurringTransactionRepository.existsByCategoryIdAndUserId(id, userId)) {
            throw categoryInUseConflict();
        }

        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw categoryInUseConflict();
        }
    }

    private Category getOwnedCategory(Long id) {
        User user = getCurrentUser();
        return categoryRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
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

    private String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException categoryConflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Category already exists");
    }

    private ResponseStatusException categoryInUseConflict() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Category cannot be deleted because it is used by existing transactions."
        );
    }
}
