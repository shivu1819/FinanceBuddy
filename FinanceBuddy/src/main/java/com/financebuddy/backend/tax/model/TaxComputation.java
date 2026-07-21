package com.financebuddy.backend.tax.model;

import com.financebuddy.backend.tax.dto.TaxRegime;

import java.math.BigDecimal;

public record TaxComputation(
        TaxRegime regime,
        BigDecimal grossIncome,
        BigDecimal otherIncome,
        BigDecimal totalIncome,
        BigDecimal deductions,
        BigDecimal taxableIncome,
        BigDecimal taxBeforeRebate,
        BigDecimal rebate,
        BigDecimal cess,
        BigDecimal finalTax,
        BigDecimal monthlyTax,
        BigDecimal effectiveTaxRate
) {
}
