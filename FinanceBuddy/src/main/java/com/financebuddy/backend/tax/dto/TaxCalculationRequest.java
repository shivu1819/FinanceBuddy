package com.financebuddy.backend.tax.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Pattern;
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
public class TaxCalculationRequest {

    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "Financial year must use YYYY-YY format")
    private String financialYear;

    @NotNull(message = "Annual income is required")
    @PositiveOrZero(message = "Annual income must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Annual income must be a valid monetary amount")
    private BigDecimal annualIncome;

    @PositiveOrZero(message = "Other income must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Other income must be a valid monetary amount")
    private BigDecimal otherIncome;

    @PositiveOrZero(message = "Total deductions must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Total deductions must be a valid monetary amount")
    private BigDecimal totalDeductions;

    @NotNull(message = "Tax regime is required")
    @JsonAlias("regime")
    private TaxRegime taxRegime;

    @PositiveOrZero(message = "Age must not be negative")
    @Max(value = 120, message = "Age must not exceed 120")
    private Integer age;

    @PositiveOrZero(message = "Section 80C deduction must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Section 80C deduction must be a valid monetary amount")
    private BigDecimal section80C;

    @PositiveOrZero(message = "Section 80D deduction must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Section 80D deduction must be a valid monetary amount")
    private BigDecimal section80D;

    @PositiveOrZero(message = "NPS deduction must not be negative")
    @Digits(integer = 13, fraction = 2, message = "NPS deduction must be a valid monetary amount")
    @JsonAlias("nps")
    private BigDecimal nps80Ccd;

    @PositiveOrZero(message = "Home loan interest must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Home loan interest must be a valid monetary amount")
    private BigDecimal homeLoanInterest;

    @PositiveOrZero(message = "Professional tax must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Professional tax must be a valid monetary amount")
    private BigDecimal professionalTax;

    @PositiveOrZero(message = "Basic salary must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Basic salary must be a valid monetary amount")
    private BigDecimal basicSalary;

    @PositiveOrZero(message = "HRA received must not be negative")
    @Digits(integer = 13, fraction = 2, message = "HRA received must be a valid monetary amount")
    @JsonAlias("hra")
    private BigDecimal hraReceived;

    @PositiveOrZero(message = "Rent paid must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Rent paid must be a valid monetary amount")
    private BigDecimal rentPaid;

    private CityType cityType;

    @PositiveOrZero(message = "Employer NPS contribution must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Employer NPS contribution must be a valid monetary amount")
    private BigDecimal employerNpsContribution;

    @PositiveOrZero(message = "Standard deduction must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Standard deduction must be a valid monetary amount")
    private BigDecimal standardDeduction;
}
