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
public class TaxRecommendationDTO {

    private BigDecimal differenceInTax;

    private BigDecimal amountSaved;

    private TaxRegime betterRegime;

    private String reasonForRecommendation;
}
