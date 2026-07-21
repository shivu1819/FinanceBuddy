package com.financebuddy.backend.tax.mapper;

import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxCalculationResponse;
import com.financebuddy.backend.tax.dto.TaxDeductionResponse;
import com.financebuddy.backend.tax.dto.TaxRegime;
import com.financebuddy.backend.tax.dto.TaxSlabResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TaxAnalyzerMapper {

    private static final String PLACEHOLDER_MESSAGE = "Placeholder response. Tax calculation logic will be implemented later.";

    public TaxCalculationResponse toPlaceholderCalculationResponse(TaxCalculationRequest request) {
        BigDecimal deductions = request.getTotalDeductions() == null ? BigDecimal.ZERO : request.getTotalDeductions();
        BigDecimal taxableIncome = request.getAnnualIncome().subtract(deductions).max(BigDecimal.ZERO);

        return TaxCalculationResponse.builder()
                .grossIncome(request.getAnnualIncome())
                .taxableIncome(taxableIncome)
                .taxAmount(BigDecimal.ZERO)
                .effectiveTaxRate(BigDecimal.ZERO)
                .taxRegime(request.getTaxRegime() == null ? TaxRegime.NEW : request.getTaxRegime())
                .message(PLACEHOLDER_MESSAGE)
                .build();
    }

    public List<TaxSlabResponse> toPlaceholderTaxSlabs() {
        return List.of(
                TaxSlabResponse.builder()
                        .regime("NEW")
                        .minIncome(BigDecimal.ZERO)
                        .maxIncome(new BigDecimal("400000.00"))
                        .taxRate(BigDecimal.ZERO)
                        .description("New regime: income up to 4,00,000")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("NEW")
                        .minIncome(new BigDecimal("400000.00"))
                        .maxIncome(new BigDecimal("800000.00"))
                        .taxRate(new BigDecimal("5.00"))
                        .description("New regime: 5% slab")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("NEW")
                        .minIncome(new BigDecimal("800000.00"))
                        .maxIncome(new BigDecimal("1200000.00"))
                        .taxRate(new BigDecimal("10.00"))
                        .description("New regime: 10% slab")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("NEW")
                        .minIncome(new BigDecimal("1200000.00"))
                        .maxIncome(new BigDecimal("1600000.00"))
                        .taxRate(new BigDecimal("15.00"))
                        .description("New regime: 15% slab")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("NEW")
                        .minIncome(new BigDecimal("1600000.00"))
                        .maxIncome(new BigDecimal("2000000.00"))
                        .taxRate(new BigDecimal("20.00"))
                        .description("New regime: 20% slab")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("NEW")
                        .minIncome(new BigDecimal("2000000.00"))
                        .maxIncome(new BigDecimal("2400000.00"))
                        .taxRate(new BigDecimal("25.00"))
                        .description("New regime: 25% slab")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("NEW")
                        .minIncome(new BigDecimal("2400000.00"))
                        .maxIncome(null)
                        .taxRate(new BigDecimal("30.00"))
                        .description("New regime: 30% slab")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("OLD")
                        .minIncome(BigDecimal.ZERO)
                        .maxIncome(new BigDecimal("250000.00"))
                        .taxRate(BigDecimal.ZERO)
                        .description("Old regime: basic exemption for non-senior citizens")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("OLD")
                        .minIncome(new BigDecimal("250000.00"))
                        .maxIncome(new BigDecimal("500000.00"))
                        .taxRate(new BigDecimal("5.00"))
                        .description("Old regime: 5% slab")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("OLD")
                        .minIncome(new BigDecimal("500000.00"))
                        .maxIncome(new BigDecimal("1000000.00"))
                        .taxRate(new BigDecimal("20.00"))
                        .description("Old regime: 20% slab")
                        .build(),
                TaxSlabResponse.builder()
                        .regime("OLD")
                        .minIncome(new BigDecimal("1000000.00"))
                        .maxIncome(null)
                        .taxRate(new BigDecimal("30.00"))
                        .description("Old regime: 30% slab")
                        .build()
        );
    }

    public List<TaxDeductionResponse> toPlaceholderDeductions() {
        return List.of(
                TaxDeductionResponse.builder()
                        .code("80C")
                        .title("Section 80C")
                        .description("Old regime deduction for eligible investments and payments")
                        .maxAmount(new BigDecimal("150000.00"))
                        .applicableRegime("OLD")
                        .build(),
                TaxDeductionResponse.builder()
                        .code("80D")
                        .title("Medical Insurance")
                        .description("Old regime deduction for medical insurance premiums")
                        .maxAmount(new BigDecimal("25000.00"))
                        .applicableRegime("OLD")
                        .build(),
                TaxDeductionResponse.builder()
                        .code("80CCD")
                        .title("NPS")
                        .description("Additional old regime deduction for NPS contribution")
                        .maxAmount(new BigDecimal("50000.00"))
                        .applicableRegime("OLD")
                        .build(),
                TaxDeductionResponse.builder()
                        .code("24B")
                        .title("Home Loan Interest")
                        .description("Old regime deduction for self-occupied home loan interest")
                        .maxAmount(new BigDecimal("200000.00"))
                        .applicableRegime("OLD")
                        .build(),
                TaxDeductionResponse.builder()
                        .code("STANDARD_DEDUCTION")
                        .title("Standard Deduction")
                        .description("Standard deduction support")
                        .maxAmount(new BigDecimal("75000.00"))
                        .applicableRegime("OLD_AND_NEW")
                        .build()
        );
    }
}
