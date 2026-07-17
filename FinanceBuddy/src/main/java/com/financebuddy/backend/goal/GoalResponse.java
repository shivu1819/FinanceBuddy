package com.financebuddy.backend.goal;

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
public class GoalResponse {

    private Long id;

    private String title;

    private String description;

    private BigDecimal targetAmount;

    private BigDecimal savedAmount;

    private BigDecimal remainingAmount;

    private Integer progressPercentage;

    private LocalDate targetDate;

    private GoalStatus status;
}
