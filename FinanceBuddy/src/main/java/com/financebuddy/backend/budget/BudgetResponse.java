package com.financebuddy.backend.budget;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetResponse {

    private Long id;

    private Long categoryId;

    private String categoryName;

    private BigDecimal monthlyLimit;

    private BigDecimal spent;

    private BigDecimal remaining;

    private Integer percentageUsed;

    private BudgetStatus status;

    private YearMonth budgetMonth;
}
