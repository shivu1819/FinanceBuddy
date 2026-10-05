package com.financebuddy.backend.billsplit;

import jakarta.validation.constraints.Digits;
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
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {
    @NotBlank(message = "Item name is required")
    @Size(max = 150, message = "Item name must not exceed 150 characters")
    private String itemName;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    @Digits(integer = 12, fraction = 3, message = "Quantity must be valid")
    private BigDecimal quantity;

    @NotNull(message = "Unit price is required")
    @PositiveOrZero(message = "Unit price must not be negative")
    @Digits(integer = 13, fraction = 2, message = "Unit price must be a valid monetary amount")
    private BigDecimal unitPrice;

    private List<@NotBlank(message = "Participant name is required") String> participantNames;
}
