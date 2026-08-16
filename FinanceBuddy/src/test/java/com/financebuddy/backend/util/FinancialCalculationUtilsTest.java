package com.financebuddy.backend.util;

import com.financebuddy.backend.budget.BudgetStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinancialCalculationUtilsTest {

    @Test
    void budgetCalculationUsesRequestedThresholdsAndNeverReturnsNegativeRemaining() {
        var safe = FinancialCalculationUtils.calculateBudget(money("1000.00"), money("799.99"));
        var warningAtEighty = FinancialCalculationUtils.calculateBudget(money("1000.00"), money("800.00"));
        var warningAtOneHundred = FinancialCalculationUtils.calculateBudget(money("1000.00"), money("1000.00"));
        var exceeded = FinancialCalculationUtils.calculateBudget(money("1000.00"), money("1200.00"));

        assertEquals(BudgetStatus.SAFE, safe.status());
        assertEquals(BudgetStatus.WARNING, warningAtEighty.status());
        assertEquals(BudgetStatus.WARNING, warningAtOneHundred.status());
        assertEquals(BudgetStatus.EXCEEDED, exceeded.status());
        assertEquals(120, exceeded.usagePercentage());
        assertEquals(0, exceeded.remainingAmount().compareTo(BigDecimal.ZERO));
    }

    @Test
    void goalProgressIsAlwaysBetweenZeroAndOneHundred() {
        assertEquals(0, FinancialCalculationUtils.calculateGoalProgress(money("-10.00"), money("100.00")));
        assertEquals(25, FinancialCalculationUtils.calculateGoalProgress(money("25.00"), money("100.00")));
        assertEquals(100, FinancialCalculationUtils.calculateGoalProgress(money("125.00"), money("100.00")));
    }

    @Test
    void dashboardSummaryUsesIncomeMinusExpenseForBalanceAndSavings() {
        var summary = FinancialCalculationUtils.calculateDashboardSummary(
                money("5000.00"),
                money("1250.00")
        );

        assertEquals(0, summary.currentBalance().compareTo(money("3750.00")));
        assertEquals(0, summary.totalSavings().compareTo(money("3750.00")));
        assertTrue(summary.totalIncome().compareTo(summary.totalExpense()) > 0);
    }

    private BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
