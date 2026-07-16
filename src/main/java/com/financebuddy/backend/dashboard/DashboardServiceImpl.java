package com.financebuddy.backend.dashboard;

import com.financebuddy.backend.dto.DashboardCategoryExpenseResponse;
import com.financebuddy.backend.dto.DashboardRecentTransactionResponse;
import com.financebuddy.backend.dto.DashboardSummaryResponse;
import com.financebuddy.backend.entity.Transaction;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_TRANSACTION_LIMIT = 5;

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        return getDashboardSummary();
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        User user = getCurrentUser();
        Long userId = user.getId();

        BigDecimal totalIncome = transactionRepository.getTotalIncomeByUserId(userId)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalExpense = transactionRepository.getTotalExpenseByUserId(userId)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalBalance = totalIncome.subtract(totalExpense);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalBalance(totalBalance)
                .totalSavings(totalBalance)
                .totalTransactions(transactionRepository.countTransactionsByUserId(userId))
                .totalCategories(categoryRepository.countCategoriesByUserId(userId))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardRecentTransactionResponse> getRecentTransactions() {
        User user = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findLatestTransactionsByUserId(
                user.getId(),
                PageRequest.of(0, RECENT_TRANSACTION_LIMIT)
        );

        return transactions.stream()
                .map(this::mapToRecentTransactionResponse)
                .toList();
    }

    @Override
    public List<DashboardCategoryExpenseResponse> getCategoryExpenses() {
        return List.of();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    private DashboardRecentTransactionResponse mapToRecentTransactionResponse(Transaction transaction) {
        return DashboardRecentTransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .transactionType(DashboardRecentTransactionResponse.TransactionType.valueOf(
                        transaction.getTransactionType()
                ))
                .transactionDate(transaction.getTransactionDate())
                .categoryName(transaction.getCategory().getName())
                .build();
    }
}
