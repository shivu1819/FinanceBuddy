package com.financebuddy.backend.tax.calculator;

import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.exception.TaxAnalyzerException;
import com.financebuddy.backend.tax.model.TaxSlab;
import com.financebuddy.backend.tax.service.TaxSlabProvider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public abstract class AbstractTaxRegimeCalculator implements TaxRegimeCalculator {

    protected static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    protected static final BigDecimal CESS_RATE = new BigDecimal("0.04");
    protected static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
    protected static final BigDecimal TWELVE_MONTHS = new BigDecimal("12.00");

    private final TaxSlabProvider taxSlabProvider;

    protected AbstractTaxRegimeCalculator(TaxSlabProvider taxSlabProvider) {
        this.taxSlabProvider = taxSlabProvider;
    }

    protected BigDecimal totalIncome(TaxCalculationRequest request) {
        return money(request.getAnnualIncome()).add(money(request.getOtherIncome()));
    }

    protected BigDecimal slabTax(BigDecimal taxableIncome, TaxRegime regime, Integer age) {
        if (taxableIncome == null || taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new TaxAnalyzerException("Taxable income must not be negative.");
        }

        List<TaxSlab> slabs = taxSlabProvider.getSlabs(regime, age);
        BigDecimal tax = BigDecimal.ZERO;

        for (TaxSlab slab : slabs) {
            if (taxableIncome.compareTo(slab.lowerLimit()) <= 0) {
                continue;
            }

            BigDecimal slabUpperLimit = slab.upperLimit() == null ? taxableIncome : slab.upperLimit();
            BigDecimal taxableInSlab = taxableIncome.min(slabUpperLimit).subtract(slab.lowerLimit());

            if (taxableInSlab.compareTo(BigDecimal.ZERO) > 0) {
                tax = tax.add(taxableInSlab.multiply(slab.rate()));
            }
        }

        return scale(tax);
    }

    protected BigDecimal cess(BigDecimal taxAfterRebate) {
        return scale(money(taxAfterRebate).multiply(CESS_RATE));
    }

    protected BigDecimal monthlyTax(BigDecimal finalTax) {
        return money(finalTax).divide(TWELVE_MONTHS, 2, RoundingMode.HALF_UP);
    }

    protected BigDecimal effectiveTaxRate(BigDecimal finalTax, BigDecimal grossIncome) {
        if (money(grossIncome).compareTo(BigDecimal.ZERO) == 0) {
            return ZERO;
        }

        return money(finalTax).multiply(ONE_HUNDRED).divide(grossIncome, 2, RoundingMode.HALF_UP);
    }

    protected BigDecimal cap(BigDecimal value, BigDecimal maxValue) {
        return money(value).min(maxValue);
    }

    protected BigDecimal money(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    protected BigDecimal scale(BigDecimal value) {
        return money(value).setScale(2, RoundingMode.HALF_UP);
    }
}
