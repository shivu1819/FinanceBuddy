package com.financebuddy.backend.recurring;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringTransactionRequest {
    @NotNull @Positive @Digits(integer = 13, fraction = 2) private BigDecimal amount;
    @NotBlank @Pattern(regexp = "INCOME|EXPENSE") private String transactionType;
    @NotNull @Positive private Long categoryId;
    @Positive private Long bankAccountId;
    @Size(max = 255) private String description;
    @NotBlank @Pattern(regexp = "DAILY|WEEKLY|MONTHLY|YEARLY") private String frequency;
    @NotNull private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
