package com.financebuddy.backend.tax.controller;

import com.financebuddy.backend.tax.dto.ComparisonResponseDTO;
import com.financebuddy.backend.tax.dto.TaxCalculationRequest;
import com.financebuddy.backend.tax.dto.TaxCalculationResponse;
import com.financebuddy.backend.tax.dto.TaxDeductionResponse;
import com.financebuddy.backend.tax.dto.TaxSlabResponse;
import com.financebuddy.backend.tax.service.TaxAnalyzerService;
import com.financebuddy.backend.tax.service.TaxComparisonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxAnalyzerController {

    private final TaxAnalyzerService taxAnalyzerService;
    private final TaxComparisonService taxComparisonService;

    @PostMapping("/calculate")
    public ResponseEntity<TaxCalculationResponse> calculateTax(
            @Valid @RequestBody TaxCalculationRequest request
    ) {
        return ResponseEntity.ok(taxAnalyzerService.calculateTax(request));
    }

    @PostMapping("/compare")
    public ResponseEntity<ComparisonResponseDTO> compareTaxRegimes(
            @Valid @RequestBody TaxCalculationRequest request
    ) {
        return ResponseEntity.ok(taxComparisonService.compareRegimes(request));
    }

    @GetMapping("/slabs")
    public ResponseEntity<List<TaxSlabResponse>> getTaxSlabs() {
        return ResponseEntity.ok(taxAnalyzerService.getTaxSlabs());
    }

    @GetMapping("/deductions")
    public ResponseEntity<List<TaxDeductionResponse>> getDeductions() {
        return ResponseEntity.ok(taxAnalyzerService.getDeductions());
    }
}
