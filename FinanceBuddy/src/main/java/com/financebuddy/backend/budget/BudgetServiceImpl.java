package com.financebuddy.backend.budget;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.util.FinancialCalculationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
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

        try {
            return mapToResponseWithAnalytics(budgetRepository.saveAndFlush(budget));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Budget already exists for this month."
            );
        }
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found."));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found."
                ));
    }

    private BudgetResponse mapToResponseWithAnalytics(Budget budget) {
        BigDecimal spent = calculateSpent(budget);
        FinancialCalculationUtils.BudgetCalculation calculation =
                FinancialCalculationUtils.calculateBudget(budget.getMonthlyLimit(), spent);

        if (budget.getStatus() != calculation.status()) {
            budget.setStatus(calculation.status());
            budget.setUpdatedAt(LocalDateTime.now());
        }

        return BudgetResponse.builder()
                .id(budget.getId())
                .monthlyLimit(calculation.budgetAmount())
                .spent(calculation.spentAmount())
                .remaining(calculation.remainingAmount())
                .percentageUsed(calculation.usagePercentage())
                .status(calculation.status())
                .budgetMonth(budget.getBudgetMonth())
                .build();
    }

    private BigDecimal calculateSpent(Budget budget) {
        return transactionRepository.sumAmountByUserIdAndTransactionTypeAndDateBetween(
                budget.getUser().getId(),
                "EXPENSE",
                budget.getBudgetMonth().atDay(1),
                budget.getBudgetMonth().atEndOfMonth()
        );
    }
}
