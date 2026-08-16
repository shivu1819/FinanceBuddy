package com.financebuddy.backend.dashboard;

import com.financebuddy.backend.budget.Budget;
import com.financebuddy.backend.budget.BudgetRepository;
import com.financebuddy.backend.budget.BudgetStatus;
import com.financebuddy.backend.dto.CategoryResponse;
import com.financebuddy.backend.dto.DashboardCategoryExpenseResponse;
import com.financebuddy.backend.dto.DashboardMonthlyChartResponse;
import com.financebuddy.backend.dto.DashboardRecentTransactionResponse;
import com.financebuddy.backend.dto.DashboardSummaryResponse;
import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.Transaction;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.util.FinancialCalculationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_TRANSACTION_LIMIT = 10;
    private static final DateTimeFormatter RECENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        return getDashboardSummary();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        User user = getCurrentUser();
        Long userId = user.getId();
        TransactionRepository.TransactionTotalsProjection totals =
                transactionRepository.getTransactionTotalsByUserId(userId);
        BigDecimal totalIncome = totals == null ? BigDecimal.ZERO : totals.getTotalIncome();
        BigDecimal totalExpense = totals == null ? BigDecimal.ZERO : totals.getTotalExpense();
        FinancialCalculationUtils.DashboardCalculation dashboard =
                FinancialCalculationUtils.calculateDashboardSummary(totalIncome, totalExpense);
        BudgetAnalytics budget = getCurrentBudgetAnalytics(user);

        return DashboardSummaryResponse.builder()
                .totalIncome(dashboard.totalIncome())
                .totalExpense(dashboard.totalExpense())
                .totalBalance(dashboard.currentBalance())
                .totalSavings(dashboard.totalSavings())
                .totalTransactions(transactionRepository.countTransactionsByUserId(userId))
                .totalCategories(categoryRepository.countCategoriesByUserId(userId))
                .monthlyBudget(budget.monthlyBudget())
                .spentBudget(budget.spentBudget())
                .remainingBudget(budget.remainingBudget())
                .budgetUsagePercentage(budget.budgetUsagePercentage())
                .budgetStatus(budget.budgetStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardRecentTransactionResponse> getRecentTransactions() {
        User user = getCurrentUser();
        return transactionRepository.findLatestTransactionsByUserId(
                        user.getId(),
                        PageRequest.of(0, RECENT_TRANSACTION_LIMIT)
                )
                .stream()
                .map(this::mapToRecentTransactionResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardCategoryExpenseResponse> getCategoryExpenses() {
        List<TransactionRepository.CategoryExpenseProjection> expenses =
                transactionRepository.getCategoryExpensesByUserId(getCurrentUser().getId());
        BigDecimal totalExpense = expenses.stream()
                .map(TransactionRepository.CategoryExpenseProjection::getTotalAmount)
                .map(FinancialCalculationUtils::amountOrZero)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return expenses.stream()
                .map(expense -> DashboardCategoryExpenseResponse.builder()
                        .categoryName(expense.getCategoryName())
                        .categoryColor(expense.getCategoryColor())
                        .categoryIcon(expense.getCategoryIcon())
                        .totalAmount(FinancialCalculationUtils.amountOrZero(expense.getTotalAmount()))
                        .totalExpense(FinancialCalculationUtils.amountOrZero(expense.getTotalAmount()))
                        .percentage(FinancialCalculationUtils.calculatePercentageValue(
                                expense.getTotalAmount(),
                                totalExpense,
                                2,
                                true
                        ))
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DashboardMonthlyChartResponse> getMonthlyChart() {
        return transactionRepository.getMonthlyCashFlowByUserId(getCurrentUser().getId())
                .stream()
                .map(month -> {
                    BigDecimal income = FinancialCalculationUtils.amountOrZero(month.getIncome());
                    BigDecimal expense = FinancialCalculationUtils.amountOrZero(month.getExpense());
                    return DashboardMonthlyChartResponse.builder()
                            .month(YearMonth.of(month.getYearValue(), month.getMonthValue()).toString())
                            .income(income)
                            .expense(expense)
                            .savings(FinancialCalculationUtils.calculateSavings(income, expense))
                            .build();
                })
                .toList();
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        UNAUTHORIZED,
                        "Authenticated user not found."
                ));
    }

    private DashboardRecentTransactionResponse mapToRecentTransactionResponse(Transaction transaction) {
        Category category = transaction.getCategory();
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .color(category.getColor())
                .icon(category.getIcon())
                .build();

        return DashboardRecentTransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .transactionType(DashboardRecentTransactionResponse.TransactionType.valueOf(
                        transaction.getTransactionType()
                ))
                .transactionDate(transaction.getTransactionDate())
                .formattedDate(transaction.getTransactionDate().format(RECENT_DATE_FORMAT))
                .category(categoryResponse)
                .categoryName(category.getName())
                .categoryColor(category.getColor())
                .categoryIcon(category.getIcon())
                .build();
    }

    private BudgetAnalytics getCurrentBudgetAnalytics(User user) {
        return budgetRepository.findByUserAndBudgetMonth(user, YearMonth.now())
                .map(this::calculateBudgetAnalytics)
                .orElseGet(() -> new BudgetAnalytics(
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        0,
                        BudgetStatus.SAFE
                ));
    }

    private BudgetAnalytics calculateBudgetAnalytics(Budget budget) {
        BigDecimal spent = transactionRepository.sumAmountByUserIdAndTransactionTypeAndDateBetween(
                budget.getUser().getId(),
                "EXPENSE",
                budget.getBudgetMonth().atDay(1),
                budget.getBudgetMonth().atEndOfMonth()
        );
        FinancialCalculationUtils.BudgetCalculation calculation =
                FinancialCalculationUtils.calculateBudget(budget.getMonthlyLimit(), spent);

        return new BudgetAnalytics(
                calculation.budgetAmount(),
                calculation.spentAmount(),
                calculation.remainingAmount(),
                calculation.usagePercentage(),
                calculation.status()
        );
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
