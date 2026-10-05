package com.financebuddy.backend.tax;

import com.financebuddy.backend.tax.calculator.NewRegimeCalculator;
import com.financebuddy.backend.tax.calculator.OldRegimeCalculator;
import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.dto.TaxComparisonRequest;
import com.financebuddy.backend.tax.dto.ComparisonResponseDTO;
import com.financebuddy.backend.tax.service.impl.DefaultTaxSlabProvider;
import com.financebuddy.backend.tax.service.impl.TaxCalculationServiceImpl;
import com.financebuddy.backend.tax.service.impl.TaxComparisonServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxCalculationServiceTest {

    private final TaxCalculationServiceImpl service = new TaxCalculationServiceImpl(
            new OldRegimeCalculator(new DefaultTaxSlabProvider()),
            new NewRegimeCalculator(new DefaultTaxSlabProvider()),
            new com.financebuddy.backend.tax.service.TaxRequestValidator()
    );

    private final TaxComparisonServiceImpl comparisonService = new TaxComparisonServiceImpl(service);

    @Test
    void zeroIncomeProducesZeroTax() {
        assertTax(TaxRegime.OLD, "0", "0");
        assertTax(TaxRegime.NEW, "0", "0");
    }

    @Test
    void selectedFinancialYearUsesNewRegimeRebateAtTenLakhs() {
        assertTax(TaxRegime.NEW, "1000000", "0");
        assertTax(TaxRegime.OLD, "1000000", "117000");
    }

    @Test
    void newRegimeTaxAboveRebateThresholdIsCalculated() {
        assertTax(TaxRegime.NEW, "1500000", "109200");
    }

    @Test
    void oldRegimeDeductionsReduceTaxableIncome() {
        TaxCalculationResponseAssertions.assertValues(
                service.calculate(TaxCalculationRequest.builder()
                        .annualIncome(new BigDecimal("1000000"))
                        .totalDeductions(new BigDecimal("150000"))
                        .taxRegime(TaxRegime.OLD)
                        .build()),
                "850000", "85800"
        );
    }

    @Test
    void comparisonUsesTheSameCalculatorsAndRecommendsLowerTax() {
        ComparisonResponseDTO response = comparisonService.compareRegimes(TaxComparisonRequest.builder()
                .annualIncome(new BigDecimal("1000000"))
                .build());

        assertEquals(new BigDecimal("117000.00"), response.getOldRegime().getFinalTax());
        assertEquals(new BigDecimal("0.00"), response.getNewRegime().getFinalTax());
        assertEquals(TaxRegime.NEW, response.getComparison().getBetterRegime());
        assertEquals(new BigDecimal("117000.00"), response.getComparison().getAmountSaved());
    }

    private void assertTax(TaxRegime regime, String income, String tax) {
        TaxCalculationResponseAssertions.assertValues(
                service.calculate(TaxCalculationRequest.builder()
                        .annualIncome(new BigDecimal(income))
                        .taxRegime(regime)
                        .build()),
                income, tax
        );
    }

    private static final class TaxCalculationResponseAssertions {
        private static void assertValues(com.financebuddy.backend.tax.dto.TaxCalculationResponse response,
                                         String taxableIncome, String finalTax) {
            assertEquals(new BigDecimal(taxableIncome).setScale(2), response.getTaxableIncome());
            assertEquals(new BigDecimal(finalTax).setScale(2), response.getFinalTax());
        }
    }
}
