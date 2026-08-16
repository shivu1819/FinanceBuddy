package com.financebuddy.backend.util;

import com.financebuddy.backend.budget.BudgetStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class FinancialCalculationUtils {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal WARNING_THRESHOLD = new BigDecimal("0.80");

    private FinancialCalculationUtils() {
    }

    public static DashboardCalculation calculateDashboardSummary(
            BigDecimal totalIncome,
            BigDecimal totalExpense
    ) {
        BigDecimal income = amountOrZero(totalIncome);
        BigDecimal expense = amountOrZero(totalExpense);
        BigDecimal balance = income.subtract(expense);

        return new DashboardCalculation(income, expense, balance, calculateSavings(income, expense));
    }

    public static BigDecimal calculateSavings(BigDecimal income, BigDecimal expense) {
        return amountOrZero(income).subtract(amountOrZero(expense));
    }

    public static BudgetCalculation calculateBudget(BigDecimal budgetAmount, BigDecimal spentAmount) {
        BigDecimal budget = nonNegative(budgetAmount);
        BigDecimal spent = nonNegative(spentAmount);
        BigDecimal remaining = budget.subtract(spent).max(BigDecimal.ZERO);
        int usagePercentage = calculatePercentage(spent, budget, false);

        BudgetStatus status = BudgetStatus.SAFE;
        if (budget.compareTo(BigDecimal.ZERO) > 0) {
            if (spent.compareTo(budget) > 0) {
                status = BudgetStatus.EXCEEDED;
            } else if (spent.compareTo(budget.multiply(WARNING_THRESHOLD)) >= 0) {
                status = BudgetStatus.WARNING;
            }
        }

        return new BudgetCalculation(budget, spent, remaining, usagePercentage, status);
    }

    public static int calculateGoalProgress(BigDecimal savedAmount, BigDecimal targetAmount) {
        return calculatePercentage(savedAmount, targetAmount, true);
    }

    public static int calculatePercentage(
            BigDecimal part,
            BigDecimal whole,
            boolean capAtOneHundred
    ) {
        BigDecimal percentage = calculatePercentageValue(part, whole, 0, capAtOneHundred);
        return percentage.min(BigDecimal.valueOf(Integer.MAX_VALUE)).intValue();
    }

    public static BigDecimal calculatePercentageValue(
            BigDecimal part,
            BigDecimal whole,
            int scale,
            boolean capAtOneHundred
    ) {
        BigDecimal normalizedPart = nonNegative(part);
        BigDecimal normalizedWhole = nonNegative(whole);

        if (normalizedWhole.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }

        BigDecimal percentage = normalizedPart.multiply(ONE_HUNDRED)
                .divide(normalizedWhole, scale, RoundingMode.HALF_UP);

        if (capAtOneHundred) {
            percentage = percentage.min(ONE_HUNDRED);
        }

        return percentage;
    }

    public static BigDecimal nonNegative(BigDecimal value) {
        return amountOrZero(value).max(BigDecimal.ZERO);
    }

    public static BigDecimal amountOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    public record BudgetCalculation(
            BigDecimal budgetAmount,
            BigDecimal spentAmount,
            BigDecimal remainingAmount,
            int usagePercentage,
            BudgetStatus status
    ) {
    }

    public record DashboardCalculation(
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal currentBalance,
            BigDecimal totalSavings
    ) {
    }
}
