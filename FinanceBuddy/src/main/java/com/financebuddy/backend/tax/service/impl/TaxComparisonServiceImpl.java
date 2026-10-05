package com.financebuddy.backend.tax.service.impl;

import com.financebuddy.backend.tax.dto.ComparisonResponseDTO;
import com.financebuddy.backend.tax.dto.RegimeComparisonDetailDTO;
import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxCalculationResponse;
import com.financebuddy.backend.tax.dto.TaxComparisonRequest;
import com.financebuddy.backend.tax.dto.TaxRecommendationDTO;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.service.TaxCalculationService;
import com.financebuddy.backend.tax.service.TaxComparisonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TaxComparisonServiceImpl implements TaxComparisonService {

    private final TaxCalculationService taxCalculationService;

    @Override
    public ComparisonResponseDTO compareRegimes(TaxComparisonRequest request) {
        TaxCalculationResponse oldRegime = taxCalculationService.calculate(
                toCalculationRequest(request, TaxRegime.OLD)
        );
        TaxCalculationResponse newRegime = taxCalculationService.calculate(
                toCalculationRequest(request, TaxRegime.NEW)
        );

        return ComparisonResponseDTO.builder()
                .oldRegime(toDetail(oldRegime))
                .newRegime(toDetail(newRegime))
                .comparison(toRecommendation(oldRegime, newRegime))
                .build();
    }

    private RegimeComparisonDetailDTO toDetail(TaxCalculationResponse calculation) {
        return RegimeComparisonDetailDTO.builder()
                .taxableIncome(calculation.getTaxableIncome())
                .totalDeductions(calculation.getTotalDeductions())
                .taxBeforeCess(calculation.getTaxBeforeCess())
                .cess(calculation.getCess())
                .finalTax(calculation.getFinalTax())
                .monthlyTax(calculation.getMonthlyTax())
                .build();
    }

    private TaxRecommendationDTO toRecommendation(
            TaxCalculationResponse oldRegime,
            TaxCalculationResponse newRegime
    ) {
        BigDecimal differenceInTax = oldRegime.getFinalTax()
                .subtract(newRegime.getFinalTax())
                .abs();
        TaxRegime betterRegime = oldRegime.getFinalTax().compareTo(newRegime.getFinalTax()) <= 0
                ? TaxRegime.OLD
                : TaxRegime.NEW;

        return TaxRecommendationDTO.builder()
                .differenceInTax(differenceInTax)
                .amountSaved(differenceInTax)
                .betterRegime(betterRegime)
                .reasonForRecommendation(buildRecommendationReason(betterRegime, differenceInTax))
                .build();
    }

    private String buildRecommendationReason(TaxRegime betterRegime, BigDecimal amountSaved) {
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

    private TaxCalculationRequest toCalculationRequest(TaxComparisonRequest request, TaxRegime regime) {
        return TaxCalculationRequest.builder()
                .financialYear(request.getFinancialYear())
                .annualIncome(request.getAnnualIncome())
                .otherIncome(request.getOtherIncome())
                .totalDeductions(request.getTotalDeductions())
                .taxRegime(regime)
                .age(request.getAge())
                .section80C(request.getSection80C())
                .section80D(request.getSection80D())
                .nps80Ccd(request.getNps80Ccd())
                .homeLoanInterest(request.getHomeLoanInterest())
                .professionalTax(request.getProfessionalTax())
                .basicSalary(request.getBasicSalary())
                .hraReceived(request.getHraReceived())
                .rentPaid(request.getRentPaid())
                .cityType(request.getCityType())
                .employerNpsContribution(request.getEmployerNpsContribution())
                .standardDeduction(request.getStandardDeduction())
                .build();
    }
}
