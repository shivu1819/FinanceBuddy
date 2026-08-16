package com.financebuddy.backend.budget;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class BudgetRequest {

    @NotNull(message = "Monthly limit is required")
    @Positive(message = "Monthly limit must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Monthly limit must be a valid monetary amount")
    private BigDecimal monthlyLimit;

    @NotNull(message = "Budget month is required")
    private YearMonth budgetMonth;
}
