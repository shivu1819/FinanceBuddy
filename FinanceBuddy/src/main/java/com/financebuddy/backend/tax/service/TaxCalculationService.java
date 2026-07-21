package com.financebuddy.backend.tax.service;

import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxCalculationResponse;
import com.financebuddy.backend.tax.dto.TaxRegime;

import java.math.BigDecimal;

public interface TaxCalculationService {

    /**
     * Calculates detailed income tax for the selected regime.
     *
     * @param request tax calculation input
     * @return detailed tax calculation response
     */
    TaxCalculationResponse calculate(TaxCalculationRequest request);

    /**
     * Calculates taxable income after eligible deductions.
     *
     * @param request tax calculation input
     * @param regime selected tax regime
     * @return taxable income
     */
    BigDecimal calculateTaxableIncome(TaxCalculationRequest request, TaxRegime regime);

    /**
     * Calculates slab tax before cess.
     *
     * @param taxableIncome taxable income
     * @param regime selected tax regime
     * @param age taxpayer age
     * @return tax before cess
     */
    BigDecimal calculateTaxBeforeCess(BigDecimal taxableIncome, TaxRegime regime, Integer age);

    /**
     * Calculates health and education cess.
     *
     * @param taxBeforeCess tax before cess
     * @return cess amount
     */
    BigDecimal calculateCess(BigDecimal taxBeforeCess);
}
