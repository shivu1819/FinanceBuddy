package com.financebuddy.backend.tax.service;

import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.exception.TaxAnalyzerException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TaxRequestValidator {

    public void validate(TaxCalculationRequest request, boolean requireRegime) {
        if (request == null) {
            throw new TaxAnalyzerException("Tax calculation request is required.");
        }
        if (request.getAnnualIncome() == null) {
            throw new TaxAnalyzerException("Annual income is required.");
        }
        if (request.getFinancialYear() != null && !request.getFinancialYear().matches("\\d{4}-\\d{2}")) {
            throw new TaxAnalyzerException("Financial year must use YYYY-YY format.");
        }
        if (requireRegime && request.getTaxRegime() == null) {
            throw new TaxAnalyzerException("Tax regime must be OLD or NEW.");
        }
        if (request.getAge() != null && (request.getAge() < 0 || request.getAge() > 120)) {
            throw new TaxAnalyzerException("Age must be between 0 and 120.");
        }

        validateNonNegative(request.getAnnualIncome(), "Annual income");
        validateNonNegative(request.getOtherIncome(), "Other income");
        validateNonNegative(request.getTotalDeductions(), "Total deductions");
        validateNonNegative(request.getSection80C(), "Section 80C deduction");
        validateNonNegative(request.getSection80D(), "Section 80D deduction");
        validateNonNegative(request.getNps80Ccd(), "NPS deduction");
        validateNonNegative(request.getHomeLoanInterest(), "Home loan interest");
        validateNonNegative(request.getProfessionalTax(), "Professional tax");
        validateNonNegative(request.getBasicSalary(), "Basic salary");
        validateNonNegative(request.getHraReceived(), "HRA received");
        validateNonNegative(request.getRentPaid(), "Rent paid");
        validateNonNegative(request.getEmployerNpsContribution(), "Employer NPS contribution");
        validateNonNegative(request.getStandardDeduction(), "Standard deduction");

        if (isPositive(request.getTotalDeductions()) && hasItemizedDeduction(request)) {
            throw new TaxAnalyzerException(
                    "Provide either total deductions or itemized deductions, not both."
            );
        }
    }

    private boolean hasItemizedDeduction(TaxCalculationRequest request) {
        return List.of(
                        valueOrZero(request.getSection80C()),
                        valueOrZero(request.getSection80D()),
                        valueOrZero(request.getNps80Ccd()),
                        valueOrZero(request.getHomeLoanInterest()),
                        valueOrZero(request.getProfessionalTax()),
                        valueOrZero(request.getHraReceived()),
                        valueOrZero(request.getEmployerNpsContribution()),
                        valueOrZero(request.getStandardDeduction())
                )
                .stream()
                .anyMatch(this::isPositive);
    }

    private void validateNonNegative(BigDecimal value, String fieldName) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw new TaxAnalyzerException(fieldName + " must not be negative.");
        }
    }

    private boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
