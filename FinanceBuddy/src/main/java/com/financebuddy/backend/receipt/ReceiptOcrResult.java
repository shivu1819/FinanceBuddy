package com.financebuddy.backend.receipt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ReceiptOcrResult(
        String merchantName,
        LocalDate transactionDate,
        BigDecimal totalAmount,
        BigDecimal taxAmount,
        String currency,
        String suggestedCategory,
        String rawText,
        Double confidence,
        LocalTime transactionTime,
        BigDecimal subtotalAmount,
        String paymentMethod,
        List<ReceiptLineItem> items,
        Double merchantConfidence,
        Double dateConfidence,
        Double totalConfidence,
        Double taxConfidence,
        Double itemConfidence,
        Double overallConfidence,
        Boolean financiallyConsistent,
        String validationMessage,
        BigDecimal cgstAmount,
        BigDecimal sgstAmount,
        BigDecimal igstAmount,
        Double subtotalConfidence,
        Double categoryConfidence
) {

    public ReceiptOcrResult(
            String merchantName,
            LocalDate transactionDate,
            BigDecimal totalAmount,
            BigDecimal taxAmount,
            String currency,
            String suggestedCategory,
            String rawText,
            Double confidence
    ) {
        this(
                merchantName,
                transactionDate,
                totalAmount,
                taxAmount,
                currency,
                suggestedCategory,
                rawText,
                confidence,
                null,
                null,
                null,
                List.of(),
                confidence,
                transactionDate == null ? 0.0 : confidence,
                totalAmount == null ? 0.0 : confidence,
                taxAmount == null ? 0.0 : confidence,
                0.0,
                confidence,
                null,
                null,
                null,
                null,
                null,
                0.0,
                0.0
        );
    }
}
