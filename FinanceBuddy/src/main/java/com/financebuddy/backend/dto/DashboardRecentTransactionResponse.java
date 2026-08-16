package com.financebuddy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardRecentTransactionResponse {

    private Long id;

    private String description;

    private BigDecimal amount;

    private TransactionType transactionType;

    private LocalDate transactionDate;

    private String formattedDate;

    private CategoryResponse category;

    private String categoryName;

    private String categoryColor;

    private String categoryIcon;

    public enum TransactionType {
        INCOME,
        EXPENSE
    }
}
