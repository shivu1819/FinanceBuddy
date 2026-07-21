package com.financebuddy.backend.tax.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComparisonResponseDTO {

    private RegimeComparisonDetailDTO oldRegime;

    private RegimeComparisonDetailDTO newRegime;

    private TaxRecommendationDTO comparison;
}
