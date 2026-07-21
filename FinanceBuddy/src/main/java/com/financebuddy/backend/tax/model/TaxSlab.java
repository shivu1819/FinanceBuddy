package com.financebuddy.backend.tax.model;

import com.financebuddy.backend.tax.dto.TaxRegime;

import java.math.BigDecimal;

public record TaxSlab(
        TaxRegime regime,
        BigDecimal lowerLimit,
        BigDecimal upperLimit,
        BigDecimal rate
) {
}
