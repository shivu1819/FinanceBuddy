package com.financebuddy.backend.tax.service.impl;

import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxCalculationResponse;
import com.financebuddy.backend.tax.dto.TaxComparisonRequest;
import com.financebuddy.backend.tax.dto.TaxComparisonResponse;
import com.financebuddy.backend.tax.dto.TaxDeductionResponse;
import com.financebuddy.backend.tax.dto.TaxSlabResponse;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.mapper.TaxAnalyzerMapper;
import com.financebuddy.backend.tax.service.TaxCalculationService;
import com.financebuddy.backend.tax.service.TaxAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxAnalyzerServiceImpl implements TaxAnalyzerService {

    private final TaxAnalyzerMapper taxAnalyzerMapper;
    private final TaxCalculationService taxCalculationService;

    @Override
    public TaxCalculationResponse calculateTax(TaxCalculationRequest request) {
        return taxCalculationService.calculate(request);
    }

    @Override
    public TaxComparisonResponse compareTaxRegimes(TaxComparisonRequest request) {
        TaxCalculationResponse oldRegimeResponse = taxCalculationService.calculate(toCalculationRequest(request, TaxRegime.OLD));
        TaxCalculationResponse newRegimeResponse = taxCalculationService.calculate(toCalculationRequest(request, TaxRegime.NEW));

        BigDecimal oldRegimeTax = oldRegimeResponse.getFinalTax();
        BigDecimal newRegimeTax = newRegimeResponse.getFinalTax();
        boolean newRegimeIsBetter = newRegimeTax.compareTo(oldRegimeTax) <= 0;

        return TaxComparisonResponse.builder()
                .oldRegimeTax(oldRegimeTax)
                .newRegimeTax(newRegimeTax)
                .recommendedRegime(newRegimeIsBetter ? "NEW" : "OLD")
                .estimatedSavings(newRegimeIsBetter
                        ? oldRegimeTax.subtract(newRegimeTax)
                        : newRegimeTax.subtract(oldRegimeTax))
                .message("Tax regime comparison completed successfully.")
                .build();
    }

    @Override
    public List<TaxSlabResponse> getTaxSlabs() {
        return taxAnalyzerMapper.toPlaceholderTaxSlabs();
    }

    @Override
    public List<TaxDeductionResponse> getDeductions() {
        return taxAnalyzerMapper.toPlaceholderDeductions();
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
