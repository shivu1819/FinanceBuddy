package com.financebuddy.backend.investment;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class InvestmentRequest {

    @NotBlank(message = "Investment name is required")
    @Size(max = 150, message = "Investment name must not exceed 150 characters")
    private String investmentName;

    @NotNull(message = "Investment type is required")
    private InvestmentType investmentType;

    @NotNull(message = "Invested amount is required")
    @Positive(message = "Invested amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Invested amount must be a valid monetary amount")
    private BigDecimal investedAmount;

    @NotNull(message = "Current value is required")
    @PositiveOrZero(message = "Current value must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Current value must be a valid monetary amount")
    private BigDecimal currentValue;

    @NotNull(message = "Investment date is required")
    private LocalDate investmentDate;

    @NotNull(message = "Expected return is required")
    @PositiveOrZero(message = "Expected return must not be negative")
    @Digits(integer = 3, fraction = 2, message = "Expected return must be a valid percentage")
    private BigDecimal expectedReturn;

    @NotNull(message = "Risk level is required")
    private RiskLevel riskLevel;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
