package com.financebuddy.backend.ai;

public record FinancialInsight(
        String category,
        String title,
        String message,
        String severity,
        String recommendation
) { }
