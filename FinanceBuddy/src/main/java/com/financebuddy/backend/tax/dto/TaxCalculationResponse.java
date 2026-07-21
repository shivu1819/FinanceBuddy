package com.financebuddy.backend.tax.dto;

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
public class TaxCalculationResponse {

    private TaxRegime regime;

    private BigDecimal grossIncome;

    private BigDecimal otherIncome;

    private BigDecimal totalIncome;

    private BigDecimal taxableIncome;

    private BigDecimal deductions;

    private BigDecimal taxBeforeRebate;

    private BigDecimal rebate;

    private BigDecimal taxBeforeCess;

    private BigDecimal cess;

    private BigDecimal finalTax;

    private BigDecimal monthlyTax;

    private BigDecimal monthlyTaxLiability;

    private BigDecimal taxAmount;

    private BigDecimal effectiveTaxRate;

    private TaxRegime taxRegime;

    private String message;
}
