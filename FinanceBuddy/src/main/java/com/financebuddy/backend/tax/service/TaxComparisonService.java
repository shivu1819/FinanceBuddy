package com.financebuddy.backend.tax.service;

import com.financebuddy.backend.tax.dto.ComparisonResponseDTO;
import com.financebuddy.backend.tax.dto.TaxComparisonRequest;

public interface TaxComparisonService {

    /**
     * Compares old and new tax regimes for the same input.
     *
     * @param request tax calculation request
     * @return detailed comparison with recommendation
     */
    ComparisonResponseDTO compareRegimes(TaxComparisonRequest request);
}
