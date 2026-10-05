package com.financebuddy.backend.report;

import lombok.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FinancialReportResponse {
    private YearMonth month;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal savings;
    private BigDecimal budgetLimit;
    private BigDecimal budgetSpent;
    private List<CategoryReport> categories;
    private List<GoalReport> goals;
    public record CategoryReport(String category, BigDecimal amount) {}
    public record GoalReport(String title, BigDecimal target, BigDecimal saved, Integer progressPercentage) {}
}
