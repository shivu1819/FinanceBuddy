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
public class RegimeComparisonDetailDTO {

    private BigDecimal taxableIncome;

    private BigDecimal totalDeductions;

    private BigDecimal taxBeforeCess;

    private BigDecimal cess;

    private BigDecimal finalTax;

    private BigDecimal monthlyTax;
}
