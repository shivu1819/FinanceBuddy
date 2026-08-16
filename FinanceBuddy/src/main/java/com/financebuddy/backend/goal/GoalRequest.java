package com.financebuddy.backend.goal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class GoalRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Target amount must be a valid monetary amount")
    private BigDecimal targetAmount;

    @PositiveOrZero(message = "Saved amount must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Saved amount must be a valid monetary amount")
    private BigDecimal savedAmount;

    @Future(message = "Target date must be in the future")
    private LocalDate targetDate;
}
