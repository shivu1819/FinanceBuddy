package com.financebuddy.backend.billsplit;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillSplitRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private LocalDate billDate;

    @NotNull(message = "Subtotal is required")
    @Positive(message = "Subtotal must be greater than zero")
    @Digits(integer = 13, fraction = 2, message = "Subtotal must be a valid monetary amount")
    private BigDecimal subtotal;

    @PositiveOrZero(message = "Tax amount must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Tax amount must be a valid monetary amount")
    private BigDecimal taxAmount;

    @PositiveOrZero(message = "Tip amount must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Tip amount must be a valid monetary amount")
    private BigDecimal tipAmount;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    @jakarta.validation.constraints.NotEmpty(message = "At least one participant is required")
    @Valid
    private List<ParticipantRequest> participants;

    @Valid
    private List<ItemRequest> items;
}
