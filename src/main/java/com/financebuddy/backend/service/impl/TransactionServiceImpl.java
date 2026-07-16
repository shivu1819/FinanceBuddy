package com.financebuddy.backend.service.impl;

import com.financebuddy.backend.dto.CategoryResponse;
import com.financebuddy.backend.dto.TransactionRequest;
import com.financebuddy.backend.dto.TransactionResponse;
import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.Transaction;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = getCurrentUser();
        Category category = getOwnedCategory(request.getCategoryId(), user);
        validateCategoryType(category, request.getTransactionType());

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .paymentMethod(request.getPaymentMethod())
                .transactionType(request.getTransactionType())
                .build();

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions() {
        User user = getCurrentUser();
        return transactionRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        return mapToResponse(getOwnedTransaction(id));
    }

    @Override
    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Transaction transaction = getOwnedTransaction(id);
        Category category = getOwnedCategory(request.getCategoryId(), transaction.getUser());
        validateCategoryType(category, request.getTransactionType());

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setCategory(category);

        return mapToResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void deleteTransaction(Long id) {
        transactionRepository.delete(getOwnedTransaction(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByType(String transactionType) {
        if (!"INCOME".equals(transactionType) && !"EXPENSE".equals(transactionType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transaction type must be INCOME or EXPENSE");
        }

        User user = getCurrentUser();
        return transactionRepository.findByUserIdAndTransactionType(user.getId(), transactionType)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByCategory(Long categoryId) {
        User user = getCurrentUser();
        getOwnedCategory(categoryId, user);

        return transactionRepository.findByUserIdAndCategoryId(user.getId(), categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Transaction getOwnedTransaction(Long id) {
        User user = getCurrentUser();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
        }

        return transaction;
    }

    private Category getOwnedCategory(Long categoryId, User user) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (category.getUser() == null || !category.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }

        return category;
    }

    private void validateCategoryType(Category category, String transactionType) {
        if (!category.getType().equals(transactionType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category type must match transaction type");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        Category category = transaction.getCategory();

        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .paymentMethod(transaction.getPaymentMethod())
                .transactionType(transaction.getTransactionType())
                .category(CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .type(category.getType())
                        .color(category.getColor())
                        .icon(category.getIcon())
                        .build())
                .build();
    }
}
