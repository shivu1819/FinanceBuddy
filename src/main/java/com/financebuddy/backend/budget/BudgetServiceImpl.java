package com.financebuddy.backend.budget;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public BudgetResponse createBudget(BudgetRequest request) {
        User user = getCurrentUser();

        budgetRepository.findByUserAndBudgetMonth(user, request.getBudgetMonth())
                .ifPresent(existingBudget -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Budget already exists for this month."
                    );
                });

        LocalDateTime now = LocalDateTime.now();
        Budget budget = Budget.builder()
                .user(user)
                .monthlyLimit(request.getMonthlyLimit())
                .budgetMonth(request.getBudgetMonth())
                .status(BudgetStatus.SAFE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return mapToResponse(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public BudgetResponse getCurrentBudget() {
        User user = getCurrentUser();
        YearMonth currentMonth = YearMonth.now();

        Budget budget = budgetRepository.findByUserAndBudgetMonth(user, currentMonth)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No budget found for current month."
                ));

        return mapToResponseWithAnalytics(budget);
    }

    @Override
    @Transactional
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {
        User user = getCurrentUser();
        Budget budget = getOwnedBudget(id, user);

        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setUpdatedAt(LocalDateTime.now());

        return mapToResponseWithAnalytics(budgetRepository.save(budget));
    }

    @Override
    @Transactional
    public void deleteBudget(Long id) {
        User user = getCurrentUser();
        Budget budget = getOwnedBudget(id, user);
        budgetRepository.delete(budget);
    }

    private Budget getOwnedBudget(Long id, User user) {
        return budgetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> {
                    if (budgetRepository.existsById(id)) {
                        return new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
                    }
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found.");
                });
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found."
                ));
    }

    private BudgetResponse mapToResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .monthlyLimit(budget.getMonthlyLimit())
                .spent(BigDecimal.ZERO)
                .remaining(budget.getMonthlyLimit())
                .percentageUsed(0)
                .status(budget.getStatus())
                .budgetMonth(budget.getBudgetMonth())
                .build();
    }

    private BudgetResponse mapToResponseWithAnalytics(Budget budget) {
        BigDecimal spent = calculateSpent(budget);
        BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);
        Integer percentageUsed = calculatePercentageUsed(spent, budget.getMonthlyLimit());
        BudgetStatus status = calculateStatus(percentageUsed);

        if (budget.getStatus() != status) {
            budget.setStatus(status);
            budget.setUpdatedAt(LocalDateTime.now());
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .monthlyLimit(budget.getMonthlyLimit())
                .spent(spent)
                .remaining(remaining)
                .percentageUsed(percentageUsed)
                .status(status)
                .budgetMonth(budget.getBudgetMonth())
                .build();
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
}
