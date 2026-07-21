package com.financebuddy.backend.tax.calculator;

import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.model.TaxComputation;

public interface TaxRegimeCalculator {

    /**
     * Calculates income tax under a specific tax regime.
     *
     * @param request validated calculation request
     * @return detailed tax computation
     */
    TaxComputation calculate(TaxCalculationRequest request);
}
