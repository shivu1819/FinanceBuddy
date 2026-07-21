package com.financebuddy.backend.tax.service.impl;

import com.financebuddy.backend.tax.calculator.NewRegimeCalculator;
import com.financebuddy.backend.tax.calculator.OldRegimeCalculator;
import com.financebuddy.backend.tax.dto.ComparisonResponseDTO;
import com.financebuddy.backend.tax.dto.RegimeComparisonDetailDTO;
import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxRecommendationDTO;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.exception.TaxAnalyzerException;
import com.financebuddy.backend.tax.model.TaxComputation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TaxComparisonServiceImpl implements com.financebuddy.backend.tax.service.TaxComparisonService {

    private final OldRegimeCalculator oldRegimeCalculator;
    private final NewRegimeCalculator newRegimeCalculator;

    /**
     * Calculates old and new regime tax independently, compares final tax, and recommends the better regime.
     *
     * @param request tax calculation request
     * @return detailed comparison response
     */
    @Override
    public ComparisonResponseDTO compareRegimes(TaxCalculationRequest request) {
        validateRequest(request);

        TaxComputation oldRegime = oldRegimeCalculator.calculate(request);
        TaxComputation newRegime = newRegimeCalculator.calculate(request);

        return ComparisonResponseDTO.builder()
                .oldRegime(toDetail(oldRegime))
                .newRegime(toDetail(newRegime))
                .comparison(toRecommendation(oldRegime, newRegime))
                .build();
    }

    private RegimeComparisonDetailDTO toDetail(TaxComputation computation) {
        return RegimeComparisonDetailDTO.builder()
                .taxableIncome(computation.taxableIncome())
                .totalDeductions(computation.deductions())
                .taxBeforeCess(computation.taxBeforeRebate().subtract(computation.rebate()).max(BigDecimal.ZERO))
                .cess(computation.cess())
                .finalTax(computation.finalTax())
                .monthlyTax(computation.monthlyTax())
                .build();
    }

    private TaxRecommendationDTO toRecommendation(TaxComputation oldRegime, TaxComputation newRegime) {
        BigDecimal differenceInTax = oldRegime.finalTax()
                .subtract(newRegime.finalTax())
                .abs();

        TaxRegime betterRegime = oldRegime.finalTax().compareTo(newRegime.finalTax()) <= 0
                ? TaxRegime.OLD
                : TaxRegime.NEW;

        return TaxRecommendationDTO.builder()
                .differenceInTax(differenceInTax)
                .amountSaved(differenceInTax)
                .betterRegime(betterRegime)
                .reasonForRecommendation(buildRecommendationReason(betterRegime, differenceInTax, oldRegime, newRegime))
                .build();
    }

    private String buildRecommendationReason(
            TaxRegime betterRegime,
            BigDecimal amountSaved,
            TaxComputation oldRegime,
            TaxComputation newRegime
    ) {
        if (amountSaved.compareTo(BigDecimal.ZERO) == 0) {
            return "Both regimes result in the same tax liability.";
        }

        if (betterRegime == TaxRegime.OLD) {
            return "Old Regime saves Rs. " + amountSaved
                    + " because deductions reduce taxable income.";
        }

        return "New Regime saves Rs. " + amountSaved
                + " because deductions are insufficient.";
    }

    private void validateRequest(TaxCalculationRequest request) {
        if (request == null) {
            throw new TaxAnalyzerException("Tax calculation request is required.");
        }

        if (request.getAnnualIncome() == null) {
            throw new TaxAnalyzerException("Annual income is required.");
        }

        if (request.getAnnualIncome().compareTo(BigDecimal.ZERO) < 0) {
            throw new TaxAnalyzerException("Annual income must not be negative.");
        }
    }
}
