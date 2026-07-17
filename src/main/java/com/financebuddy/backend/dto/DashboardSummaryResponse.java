package com.financebuddy.backend.dto;

import com.financebuddy.backend.budget.BudgetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal totalBalance;

    private BigDecimal totalSavings;

    private long totalTransactions;

    private long totalCategories;

    private BigDecimal monthlyBudget;

    private BigDecimal spentBudget;

    private BigDecimal remainingBudget;

    private Integer budgetUsagePercentage;

    private BudgetStatus budgetStatus;
}
