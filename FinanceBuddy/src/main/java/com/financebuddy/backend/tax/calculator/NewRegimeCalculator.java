package com.financebuddy.backend.tax.calculator;

import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.model.TaxComputation;
import com.financebuddy.backend.tax.service.TaxSlabProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class NewRegimeCalculator extends AbstractTaxRegimeCalculator {

    private static final BigDecimal STANDARD_DEDUCTION_LIMIT = new BigDecimal("75000.00");
    private static final BigDecimal REBATE_INCOME_LIMIT = new BigDecimal("1200000.00");
    private static final BigDecimal REBATE_LIMIT = new BigDecimal("60000.00");

    public NewRegimeCalculator(TaxSlabProvider taxSlabProvider) {
        super(taxSlabProvider);
    }

    /**
     * Calculates new-regime tax with applicable deductions, rebate, cess, and monthly liability.
     *
     * @param request validated calculation request
     * @return detailed new-regime tax computation
     */
    @Override
    public TaxComputation calculate(TaxCalculationRequest request) {
        BigDecimal grossIncome = money(request.getAnnualIncome());
        BigDecimal otherIncome = money(request.getOtherIncome());
        BigDecimal totalIncome = totalIncome(request);
        BigDecimal deductions = calculateDeductions(request);
        BigDecimal taxableIncome = scale(totalIncome.subtract(deductions).max(BigDecimal.ZERO));
        BigDecimal taxBeforeRebate = slabTax(taxableIncome, TaxRegime.NEW, request.getAge());
        BigDecimal rebate = calculateRebate(taxableIncome, taxBeforeRebate);
        BigDecimal taxAfterRebate = taxBeforeRebate.subtract(rebate).max(BigDecimal.ZERO);
        BigDecimal cess = cess(taxAfterRebate);
        BigDecimal finalTax = scale(taxAfterRebate.add(cess));

        return new TaxComputation(
                TaxRegime.NEW,
                grossIncome,
                otherIncome,
                totalIncome,
                deductions,
                taxableIncome,
                taxBeforeRebate,
                rebate,
                cess,
                finalTax,
                monthlyTax(finalTax),
                effectiveTaxRate(finalTax, grossIncome)
        );
    }

    private BigDecimal calculateDeductions(TaxCalculationRequest request) {
        BigDecimal deductions = BigDecimal.ZERO;
        deductions = deductions.add(cap(request.getStandardDeduction(), STANDARD_DEDUCTION_LIMIT));
        deductions = deductions.add(money(request.getEmployerNpsContribution()));
        return scale(deductions);
    }

    private BigDecimal calculateRebate(BigDecimal taxableIncome, BigDecimal taxBeforeRebate) {
        if (taxableIncome.compareTo(REBATE_INCOME_LIMIT) > 0) {
            return ZERO;
        }

        return taxBeforeRebate.min(REBATE_LIMIT);
    }
}
