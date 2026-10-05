package com.financebuddy.backend.ai;

import java.math.BigDecimal;

public record GoalProgressInsight(
        String title,
        BigDecimal targetAmount,
        BigDecimal savedAmount,
        Integer progressPercentage,
        String status
) { }
