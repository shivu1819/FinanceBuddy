package com.financebuddy.backend.tax.service.impl;

import com.financebuddy.backend.tax.calculator.NewRegimeCalculator;
import com.financebuddy.backend.tax.calculator.OldRegimeCalculator;
import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxCalculationResponse;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.exception.TaxAnalyzerException;
import com.financebuddy.backend.tax.model.TaxComputation;
import com.financebuddy.backend.tax.service.TaxCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TaxCalculationServiceImpl implements TaxCalculationService {

    private final OldRegimeCalculator oldRegimeCalculator;
    private final NewRegimeCalculator newRegimeCalculator;

    /**
     * Calculates detailed Indian income tax for the requested tax regime.
     *
     * @param request validated calculation request
     * @return detailed tax calculation response
     */
    @Override
    public TaxCalculationResponse calculate(TaxCalculationRequest request) {
        validateRequest(request);
        TaxRegime regime = request.getTaxRegime() == null ? TaxRegime.NEW : request.getTaxRegime();
        TaxComputation computation = calculateByRegime(request, regime);
        return toResponse(computation);
    }

    /**
     * Calculates taxable income for unit tests and internal comparison flows.
     *
     * @param request validated calculation request
     * @param regime selected tax regime
     * @return taxable income after eligible deductions
     */
    @Override
    public BigDecimal calculateTaxableIncome(TaxCalculationRequest request, TaxRegime regime) {
        validateRequest(request);
        return calculateByRegime(request, regime).taxableIncome();
    }

    /**
     * Calculates tax before cess for unit tests and internal comparison flows.
     *
     * @param taxableIncome taxable income
     * @param regime selected tax regime
     * @param age taxpayer age
     * @return slab tax before rebate and cess
     */
    @Override
    public BigDecimal calculateTaxBeforeCess(BigDecimal taxableIncome, TaxRegime regime, Integer age) {
        TaxCalculationRequest request = TaxCalculationRequest.builder()
                .annualIncome(taxableIncome)
                .taxRegime(regime)
                .age(age)
                .build();
        return calculateByRegime(request, regime).taxBeforeRebate();
    }

    /**
     * Calculates health and education cess.
     *
     * @param taxBeforeCess tax amount after rebate and before cess
     * @return cess at the configured rate
     */
    @Override
    public BigDecimal calculateCess(BigDecimal taxBeforeCess) {
        if (taxBeforeCess == null || taxBeforeCess.compareTo(BigDecimal.ZERO) < 0) {
            throw new TaxAnalyzerException("Tax before cess must not be negative.");
        }

        return taxBeforeCess.multiply(new BigDecimal("0.04")).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private TaxComputation calculateByRegime(TaxCalculationRequest request, TaxRegime regime) {
        if (regime == TaxRegime.OLD) {
            return oldRegimeCalculator.calculate(request);
        }

        return newRegimeCalculator.calculate(request);
    }

    private TaxCalculationResponse toResponse(TaxComputation computation) {
        return TaxCalculationResponse.builder()
                .regime(computation.regime())
                .grossIncome(computation.grossIncome())
                .otherIncome(computation.otherIncome())
                .totalIncome(computation.totalIncome())
                .deductions(computation.deductions())
                .taxableIncome(computation.taxableIncome())
                .taxBeforeRebate(computation.taxBeforeRebate())
                .rebate(computation.rebate())
                .taxBeforeCess(computation.taxBeforeRebate().subtract(computation.rebate()).max(BigDecimal.ZERO))
                .cess(computation.cess())
                .finalTax(computation.finalTax())
                .monthlyTax(computation.monthlyTax())
                .monthlyTaxLiability(computation.monthlyTax())
                .taxAmount(computation.finalTax())
                .effectiveTaxRate(computation.effectiveTaxRate())
                .taxRegime(computation.regime())
                .message("Tax calculation completed successfully.")
                .build();
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
