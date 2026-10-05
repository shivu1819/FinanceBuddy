package com.financebuddy.backend.ai;

import java.math.BigDecimal;

public record SpendingInsight(String category, BigDecimal amount, BigDecimal percentage) { }
