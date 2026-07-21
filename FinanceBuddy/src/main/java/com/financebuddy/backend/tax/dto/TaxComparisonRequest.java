package com.financebuddy.backend.tax.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class TaxComparisonRequest {

    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0.00", message = "Annual income must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Annual income must be a valid monetary amount")
    private BigDecimal annualIncome;

    @DecimalMin(value = "0.00", message = "Other income must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Other income must be a valid monetary amount")
    private BigDecimal otherIncome;

    @DecimalMin(value = "0.00", message = "Total deductions must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Total deductions must be a valid monetary amount")
    private BigDecimal totalDeductions;

    @Min(value = 0, message = "Age must not be negative")
    @Max(value = 120, message = "Age must not exceed 120")
    private Integer age;

    @DecimalMin(value = "0.00", message = "Section 80C deduction must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Section 80C deduction must be a valid monetary amount")
    private BigDecimal section80C;

    @DecimalMin(value = "0.00", message = "Section 80D deduction must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Section 80D deduction must be a valid monetary amount")
    private BigDecimal section80D;

    @DecimalMin(value = "0.00", message = "NPS deduction must not be negative")
    @Digits(integer = 13, fraction = 2, message = "NPS deduction must be a valid monetary amount")
    private BigDecimal nps80Ccd;

    @DecimalMin(value = "0.00", message = "Home loan interest must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Home loan interest must be a valid monetary amount")
    private BigDecimal homeLoanInterest;

    @DecimalMin(value = "0.00", message = "Professional tax must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Professional tax must be a valid monetary amount")
    private BigDecimal professionalTax;

    @DecimalMin(value = "0.00", message = "Basic salary must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Basic salary must be a valid monetary amount")
    private BigDecimal basicSalary;

    @DecimalMin(value = "0.00", message = "HRA received must not be negative")
    @Digits(integer = 13, fraction = 2, message = "HRA received must be a valid monetary amount")
    private BigDecimal hraReceived;

    @DecimalMin(value = "0.00", message = "Rent paid must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Rent paid must be a valid monetary amount")
    private BigDecimal rentPaid;

    private CityType cityType;

    @DecimalMin(value = "0.00", message = "Employer NPS contribution must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Employer NPS contribution must be a valid monetary amount")
    private BigDecimal employerNpsContribution;

    @DecimalMin(value = "0.00", message = "Standard deduction must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Standard deduction must be a valid monetary amount")
    private BigDecimal standardDeduction;
}
