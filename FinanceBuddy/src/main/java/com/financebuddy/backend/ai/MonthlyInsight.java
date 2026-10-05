package com.financebuddy.backend.ai;

import java.math.BigDecimal;

public record MonthlyInsight(String month, BigDecimal income, BigDecimal expenses, BigDecimal savings) { }
