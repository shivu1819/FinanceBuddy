package com.financebuddy.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {

    private Long id;

    private BigDecimal amount;

    private String description;

    private LocalDate transactionDate;

    private String paymentMethod;

    private String transactionType;

    private CategoryResponse category;
}
