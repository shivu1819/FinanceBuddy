package com.financebuddy.backend.recurring;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringTransactionResponse {
    private Long id;
    private BigDecimal amount;
    private String transactionType;
    private Long categoryId;
    private String categoryName;
    private Long bankAccountId;
    private String description;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextRunDate;
    private Boolean active;
}
