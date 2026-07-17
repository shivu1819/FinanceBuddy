package com.financebuddy.backend.dashboard;

import com.financebuddy.backend.budget.Budget;
import com.financebuddy.backend.budget.BudgetRepository;
import com.financebuddy.backend.budget.BudgetStatus;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_TRANSACTION_LIMIT = 5;

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    @Override
    @Transactional
    public DashboardSummaryResponse getSummary() {
        return getDashboardSummary();
    }

    @Transactional
    public DashboardSummaryResponse getDashboardSummary() {
        User user = getCurrentUser();
        Long userId = user.getId();

        BigDecimal totalIncome = transactionRepository.getTotalIncomeByUserId(userId)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalExpense = transactionRepository.getTotalExpenseByUserId(userId)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalBalance = totalIncome.subtract(totalExpense);
        BudgetAnalytics budgetAnalytics = getCurrentBudgetAnalytics(user);

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalBalance(totalBalance)
                .totalSavings(totalBalance)
                .totalTransactions(transactionRepository.countTransactionsByUserId(userId))
                .totalCategories(categoryRepository.countCategoriesByUserId(userId))
                .monthlyBudget(budgetAnalytics.monthlyBudget())
                .spentBudget(budgetAnalytics.spentBudget())
                .remainingBudget(budgetAnalytics.remainingBudget())
                .budgetUsagePercentage(budgetAnalytics.budgetUsagePercentage())
                .budgetStatus(budgetAnalytics.budgetStatus())
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

    private BudgetAnalytics getCurrentBudgetAnalytics(User user) {
        Optional<Budget> budgetOptional = budgetRepository.findByUserAndBudgetMonth(user, YearMonth.now());

        if (budgetOptional.isEmpty()) {
            return new BudgetAnalytics(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, BudgetStatus.SAFE);
        }

        Budget budget = budgetOptional.get();
        BigDecimal spent = calculateSpent(budget);
        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
        Integer percentageUsed = calculatePercentageUsed(spent, budget.getMonthlyLimit());
        BudgetStatus status = calculateStatus(percentageUsed);

        if (budget.getStatus() != status) {
            budget.setStatus(status);
            budget.setUpdatedAt(LocalDateTime.now());
        }

        return new BudgetAnalytics(budget.getMonthlyLimit(), spent, remaining, percentageUsed, status);
    }

    private BigDecimal calculateSpent(Budget budget) {
        LocalDate startDate = budget.getBudgetMonth().atDay(1);
        LocalDate endDate = budget.getBudgetMonth().atEndOfMonth();

        return transactionRepository.findByUserIdAndTransactionDateBetween(
                        budget.getUser().getId(),
                        startDate,
                        endDate
                )
                .stream()
                .filter(transaction -> "EXPENSE".equals(transaction.getTransactionType()))
                .map(transaction -> transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer calculatePercentageUsed(BigDecimal spent, BigDecimal monthlyLimit) {
        if (monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        return spent.multiply(BigDecimal.valueOf(100))
                .divide(monthlyLimit, 0, RoundingMode.HALF_UP)
                .intValue();
    }

    private BudgetStatus calculateStatus(Integer percentageUsed) {
        if (percentageUsed >= 100) {
            return BudgetStatus.EXCEEDED;
        }

        if (percentageUsed >= 80) {
            return BudgetStatus.WARNING;
        }

        return BudgetStatus.SAFE;
    }

    private record BudgetAnalytics(
            BigDecimal monthlyBudget,
            BigDecimal spentBudget,
            BigDecimal remainingBudget,
            Integer budgetUsagePercentage,
            BudgetStatus budgetStatus
    ) {
    }
}
