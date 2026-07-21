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
public class TaxSlabResponse {

    private String regime;

    private BigDecimal minIncome;

    private BigDecimal maxIncome;

    private BigDecimal taxRate;

    private String description;
}
