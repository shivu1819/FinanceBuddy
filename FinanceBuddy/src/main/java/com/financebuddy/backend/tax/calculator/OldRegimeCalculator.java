package com.financebuddy.backend.tax.calculator;

import com.financebuddy.backend.tax.dto.CityType;
import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.model.TaxComputation;
import com.financebuddy.backend.tax.service.TaxSlabProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OldRegimeCalculator extends AbstractTaxRegimeCalculator {

    private static final BigDecimal SECTION_80C_LIMIT = new BigDecimal("150000.00");
    private static final BigDecimal SECTION_80D_LIMIT = new BigDecimal("25000.00");
    private static final BigDecimal SECTION_80D_SENIOR_LIMIT = new BigDecimal("50000.00");
    private static final BigDecimal NPS_80CCD_LIMIT = new BigDecimal("50000.00");
    private static final BigDecimal HOME_LOAN_INTEREST_LIMIT = new BigDecimal("200000.00");
    private static final BigDecimal PROFESSIONAL_TAX_LIMIT = new BigDecimal("2500.00");
    private static final BigDecimal STANDARD_DEDUCTION_LIMIT = new BigDecimal("50000.00");
    private static final BigDecimal REBATE_INCOME_LIMIT = new BigDecimal("500000.00");
    private static final BigDecimal REBATE_LIMIT = new BigDecimal("12500.00");

    public OldRegimeCalculator(TaxSlabProvider taxSlabProvider) {
        super(taxSlabProvider);
    }

    /**
     * Calculates old-regime tax including eligible deductions, rebate, cess, and monthly liability.
     *
     * @param request validated calculation request
     * @return detailed old-regime tax computation
     */
    @Override
    public TaxComputation calculate(TaxCalculationRequest request) {
        BigDecimal grossIncome = money(request.getAnnualIncome());
        BigDecimal otherIncome = money(request.getOtherIncome());
        BigDecimal totalIncome = totalIncome(request);
        BigDecimal deductions = calculateDeductions(request);
        BigDecimal taxableIncome = scale(totalIncome.subtract(deductions).max(BigDecimal.ZERO));
        BigDecimal taxBeforeRebate = slabTax(taxableIncome, TaxRegime.OLD, request.getAge());
        BigDecimal rebate = calculateRebate(taxableIncome, taxBeforeRebate);
        BigDecimal taxAfterRebate = taxBeforeRebate.subtract(rebate).max(BigDecimal.ZERO);
        BigDecimal cess = cess(taxAfterRebate);
        BigDecimal finalTax = scale(taxAfterRebate.add(cess));

        return new TaxComputation(
                TaxRegime.OLD,
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
        deductions = deductions.add(cap(request.getSection80C(), SECTION_80C_LIMIT));
        deductions = deductions.add(cap(request.getSection80D(), section80DLimit(request.getAge())));
        deductions = deductions.add(cap(request.getNps80Ccd(), NPS_80CCD_LIMIT));
        deductions = deductions.add(cap(request.getHomeLoanInterest(), HOME_LOAN_INTEREST_LIMIT));
        deductions = deductions.add(cap(request.getProfessionalTax(), PROFESSIONAL_TAX_LIMIT));
        deductions = deductions.add(cap(request.getStandardDeduction(), STANDARD_DEDUCTION_LIMIT));
        deductions = deductions.add(calculateHraExemption(request));
        deductions = deductions.add(money(request.getTotalDeductions()));
        return scale(deductions);
    }

    private BigDecimal calculateRebate(BigDecimal taxableIncome, BigDecimal taxBeforeRebate) {
        if (taxableIncome.compareTo(REBATE_INCOME_LIMIT) > 0) {
            return ZERO;
        }

        return taxBeforeRebate.min(REBATE_LIMIT);
    }

    private BigDecimal section80DLimit(Integer age) {
        return age != null && age >= 60 ? SECTION_80D_SENIOR_LIMIT : SECTION_80D_LIMIT;
    }

    private BigDecimal calculateHraExemption(TaxCalculationRequest request) {
        BigDecimal hraReceived = money(request.getHraReceived());
        BigDecimal basicSalary = money(request.getBasicSalary());
        BigDecimal rentPaid = money(request.getRentPaid());

        if (hraReceived.compareTo(BigDecimal.ZERO) == 0
                || basicSalary.compareTo(BigDecimal.ZERO) == 0
                || rentPaid.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal rentMinusTenPercentBasic = rentPaid.subtract(basicSalary.multiply(new BigDecimal("0.10")));
        BigDecimal salaryPercentage = request.getCityType() == CityType.METRO
                ? new BigDecimal("0.50")
                : new BigDecimal("0.40");
        BigDecimal salaryBasedLimit = basicSalary.multiply(salaryPercentage);

        return hraReceived.min(salaryBasedLimit).min(rentMinusTenPercentBasic).max(BigDecimal.ZERO);
    }
}
