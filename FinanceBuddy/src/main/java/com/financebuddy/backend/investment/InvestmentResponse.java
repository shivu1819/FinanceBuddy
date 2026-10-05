package com.financebuddy.backend.investment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvestmentResponse {

    private Long id;
    private String investmentName;
    private InvestmentType investmentType;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private BigDecimal gainLoss;
    private LocalDate investmentDate;
    private BigDecimal expectedReturn;
    private RiskLevel riskLevel;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
