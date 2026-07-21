package com.financebuddy.backend.tax.service;

import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxCalculationResponse;
import com.financebuddy.backend.tax.dto.TaxComparisonRequest;
import com.financebuddy.backend.tax.dto.TaxComparisonResponse;
import com.financebuddy.backend.tax.dto.TaxDeductionResponse;
import com.financebuddy.backend.tax.dto.TaxSlabResponse;

import java.util.List;

public interface TaxAnalyzerService {

    /**
     * Calculates tax for one tax regime.
     *
     * @param request calculation request
     * @return detailed calculation response
     */
    TaxCalculationResponse calculateTax(TaxCalculationRequest request);

    /**
     * Compares old and new tax regimes.
     *
     * @param request comparison request
     * @return regime comparison response
     */
    TaxComparisonResponse compareTaxRegimes(TaxComparisonRequest request);

    /**
     * Returns supported tax slabs.
     *
     * @return slab metadata
     */
    List<TaxSlabResponse> getTaxSlabs();

    /**
     * Returns supported deduction metadata.
     *
     * @return deduction metadata
     */
    List<TaxDeductionResponse> getDeductions();
}
