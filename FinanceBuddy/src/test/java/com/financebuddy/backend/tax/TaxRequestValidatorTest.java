package com.financebuddy.backend.tax;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financebuddy.backend.tax.calculator.NewRegimeCalculator;
import com.financebuddy.backend.tax.calculator.OldRegimeCalculator;
import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.exception.TaxAnalyzerException;
import com.financebuddy.backend.tax.service.TaxRequestValidator;
import com.financebuddy.backend.tax.service.impl.DefaultTaxSlabProvider;
import com.financebuddy.backend.tax.service.impl.TaxCalculationServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaxRequestValidatorTest {

    private final TaxRequestValidator validator = new TaxRequestValidator();

    @Test
    void negativeIncomeAndDeductionsAreRejected() {
        TaxCalculationRequest negativeIncome = request(TaxRegime.NEW);
        negativeIncome.setAnnualIncome(new BigDecimal("-1.00"));
        assertThrows(TaxAnalyzerException.class, () -> validator.validate(negativeIncome, true));

        TaxCalculationRequest negativeDeduction = request(TaxRegime.OLD);
        negativeDeduction.setSection80C(new BigDecimal("-1.00"));
        assertThrows(TaxAnalyzerException.class, () -> validator.validate(negativeDeduction, true));
    }

    @Test
    void missingRegimeAndDuplicateDeductionInputsAreRejected() {
        TaxCalculationRequest missingRegime = request(null);
        assertThrows(TaxAnalyzerException.class, () -> validator.validate(missingRegime, true));

        TaxCalculationRequest duplicated = request(TaxRegime.OLD);
        duplicated.setTotalDeductions(new BigDecimal("1000.00"));
        duplicated.setSection80C(new BigDecimal("500.00"));
        TaxAnalyzerException exception = assertThrows(
                TaxAnalyzerException.class,
                () -> validator.validate(duplicated, true)
        );
        assertEquals("Provide either total deductions or itemized deductions, not both.", exception.getMessage());
    }

    @Test
    void regimeAndLegacyDeductionAliasesDeserializeCorrectly() throws Exception {
        TaxCalculationRequest request = new ObjectMapper().readValue(
                "{\"annualIncome\":500000,\"regime\":\"OLD\",\"nps\":1000,\"hra\":2000}",
                TaxCalculationRequest.class
        );

        assertEquals(TaxRegime.OLD, request.getTaxRegime());
        assertEquals(0, request.getNps80Ccd().compareTo(new BigDecimal("1000")));
        assertEquals(0, request.getHraReceived().compareTo(new BigDecimal("2000")));
    }

    @Test
    void calculatedTaxCannotBecomeNegative() {
        DefaultTaxSlabProvider slabProvider = new DefaultTaxSlabProvider();
        TaxCalculationServiceImpl service = new TaxCalculationServiceImpl(
                new OldRegimeCalculator(slabProvider),
                new NewRegimeCalculator(slabProvider),
                validator
        );
        TaxCalculationRequest request = request(TaxRegime.NEW);
        request.setAnnualIncome(BigDecimal.ZERO);
        request.setStandardDeduction(new BigDecimal("75000.00"));

        assertEquals(0, service.calculate(request).getFinalTax().compareTo(BigDecimal.ZERO));
    }

    private TaxCalculationRequest request(TaxRegime regime) {
        return TaxCalculationRequest.builder()
                .annualIncome(new BigDecimal("500000.00"))
                .taxRegime(regime)
                .build();
    }
}
