package com.financebuddy.backend.ai;

import java.math.BigDecimal;
import java.util.List;

public record AIInsightResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netSavings,
        BigDecimal savingsPercentage,
        String highestSpendingCategory,
        List<SpendingInsight> topSpendingCategories,
        List<MonthlyInsight> monthlyTrend,
        List<String> recommendations,
        boolean enoughData,
        String message,
        Integer healthScore,
        BigDecimal budgetLimit,
        BigDecimal budgetSpent,
        Integer budgetUtilization,
        List<FinancialInsight> insights,
        List<GoalProgressInsight> goalProgress
) { }
