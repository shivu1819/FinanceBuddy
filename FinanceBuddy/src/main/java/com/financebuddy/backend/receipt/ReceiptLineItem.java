package com.financebuddy.backend.receipt;

import java.math.BigDecimal;

public record ReceiptLineItem(
        String itemName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        Double confidence
) {
}
