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
public class TaxComparisonResponse {

    private BigDecimal oldRegimeTax;

    private BigDecimal newRegimeTax;

    private String recommendedRegime;

    private BigDecimal estimatedSavings;

    private String message;
}
