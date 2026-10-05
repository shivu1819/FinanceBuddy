package com.financebuddy.backend.service.impl;

import com.financebuddy.backend.dto.CategoryResponse;
import com.financebuddy.backend.dto.TransactionPageResponse;
import com.financebuddy.backend.dto.TransactionRequest;
import com.financebuddy.backend.dto.TransactionResponse;
import com.financebuddy.backend.dto.TransactionSortOption;
import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.BankAccount;
import com.financebuddy.backend.entity.Transaction;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.BankAccountRepository;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = getCurrentUser();
        Category category = getOwnedCategory(request.getCategoryId(), user);
        BankAccount bankAccount = getOwnedBankAccount(request.getBankAccountId(), user);
        validateCategoryType(category, request.getTransactionType());

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .bankAccount(bankAccount)
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
        BankAccount bankAccount = request.getBankAccountId() == null
                ? transaction.getBankAccount()
                : getOwnedBankAccount(request.getBankAccountId(), transaction.getUser());
        validateCategoryType(category, request.getTransactionType());

        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setCategory(category);
        transaction.setBankAccount(bankAccount);

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

    @Override
    @Transactional(readOnly = true)
    public TransactionPageResponse searchTransactions(
            String search,
            String title,
            String notes,
            Long categoryId,
            String transactionType,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minimumAmount,
            BigDecimal maximumAmount,
            String sort,
            int page,
            int size
    ) {
        validateSearchCriteria(
                categoryId,
                transactionType,
                startDate,
                endDate,
                minimumAmount,
                maximumAmount,
                page,
                size
        );

        User user = getCurrentUser();
        String normalizedType = normalizeTransactionType(transactionType);
        PageRequest pageRequest = PageRequest.of(page, size, resolveSort(TransactionSortOption.from(sort)));
        Page<Transaction> transactions = transactionRepository.searchTransactions(
                user.getId(),
                toLikeTerm(search),
                toLikeTerm(title),
                toLikeTerm(notes),
                categoryId,
                normalizedType,
                startDate,
                endDate,
                minimumAmount,
                maximumAmount,
                pageRequest
        );

        return TransactionPageResponse.builder()
                .content(transactions.getContent().stream().map(this::mapToResponse).toList())
                .page(transactions.getNumber())
                .size(transactions.getSize())
                .totalElements(transactions.getTotalElements())
                .totalPages(transactions.getTotalPages())
                .build();
    }

    private Transaction getOwnedTransaction(Long id) {
        User user = getCurrentUser();
        return transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));
    }

    private Category getOwnedCategory(Long categoryId, User user) {
        return categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
    }

    private BankAccount getOwnedBankAccount(Long bankAccountId, User user) {
        if (bankAccountId == null) {
            return null;
        }

        return bankAccountRepository.findByIdAndUserId(bankAccountId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank account not found"));
    }

    private void validateCategoryType(Category category, String transactionType) {
        if (!category.getType().equals(transactionType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category type must match transaction type");
        }
    }

    private void validateSearchCriteria(
            Long categoryId,
            String transactionType,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minimumAmount,
            BigDecimal maximumAmount,
            int page,
            int size
    ) {
        if (categoryId != null && categoryId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID must be positive.");
        }
        normalizeTransactionType(transactionType);
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must not be after end date.");
        }
        if (minimumAmount != null && minimumAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum amount must not be negative.");
        }
        if (maximumAmount != null && maximumAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum amount must not be negative.");
        }
        if (minimumAmount != null && maximumAmount != null
                && minimumAmount.compareTo(maximumAmount) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Minimum amount must not exceed maximum amount."
            );
        }
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must not be negative.");
        }
        if (size < 1 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be between 1 and 100.");
        }
    }

    private String normalizeTransactionType(String transactionType) {
        if (transactionType == null || transactionType.isBlank()) {
            return null;
        }

        String normalizedType = transactionType.trim().toUpperCase(Locale.ROOT);
        if (!"INCOME".equals(normalizedType) && !"EXPENSE".equals(normalizedType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Transaction type must be INCOME or EXPENSE."
            );
        }
        return normalizedType;
    }

    private String toLikeTerm(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private Sort resolveSort(TransactionSortOption sortOption) {
        return switch (sortOption) {
            case NEWEST -> Sort.by(
                    Sort.Order.desc("transactionDate"),
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("id")
            );
            case OLDEST -> Sort.by(
                    Sort.Order.asc("transactionDate"),
                    Sort.Order.asc("createdAt"),
                    Sort.Order.asc("id")
            );
            case HIGHEST_AMOUNT -> Sort.by(
                    Sort.Order.desc("amount"),
                    Sort.Order.desc("transactionDate"),
                    Sort.Order.desc("id")
            );
            case LOWEST_AMOUNT -> Sort.by(
                    Sort.Order.asc("amount"),
                    Sort.Order.desc("transactionDate"),
                    Sort.Order.desc("id")
            );
        };
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
                .bankAccountId(transaction.getBankAccount() == null ? null : transaction.getBankAccount().getId())
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
