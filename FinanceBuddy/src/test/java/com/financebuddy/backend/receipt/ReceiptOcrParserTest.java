package com.financebuddy.backend.receipt;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptOcrParserTest {

    @Test
    void mapsCommonReceiptFieldsWithoutInventingMissingValues() {
        ReceiptOcrResult result = ReceiptOcrParser.parse("""
                Cafe Example
                Date: 11/09/2026
                GST: ₹12.50
                Grand Total: ₹245.00
                """);

        assertEquals("Cafe Example", result.merchantName());
        assertEquals(LocalDate.of(2026, 9, 11), result.transactionDate());
        assertEquals(new BigDecimal("245.00"), result.totalAmount());
        assertEquals(new BigDecimal("12.50"), result.taxAmount());
        assertEquals("INR", result.currency());
        assertEquals("Food", result.suggestedCategory());
    }

    @Test
    void leavesUndetectedValuesNull() {
        ReceiptOcrResult result = ReceiptOcrParser.parse("Unknown Store");

        assertEquals("Unknown Store", result.merchantName());
        assertNull(result.transactionDate());
        assertNull(result.totalAmount());
        assertNull(result.taxAmount());
        assertNull(result.currency());
        assertNull(result.suggestedCategory());
    }

    @Test
    void extractsReceiptDetailsAndValidatesFinancialTotals() {
        ReceiptOcrResult result = ReceiptOcrParser.parse("""
                Fresh Cafe
                Date: 14/09/2026 10:30 AM
                Coffee x2 100.00
                Subtotal 100.00
                CGST 9.00
                SGST 9.00
                Grand Total 118.00
                Paid by UPI
                """);

        assertEquals("Fresh Cafe", result.merchantName());
        assertEquals(LocalDate.of(2026, 9, 14), result.transactionDate());
        assertEquals(new BigDecimal("100.00"), result.subtotalAmount());
        assertEquals(new BigDecimal("18.00"), result.taxAmount());
        assertEquals(new BigDecimal("118.00"), result.totalAmount());
        assertEquals("UPI", result.paymentMethod());
        assertEquals("Food", result.suggestedCategory());
        assertEquals(1, result.items().size());
        assertEquals(Boolean.TRUE, result.financiallyConsistent());
        assertEquals(2, result.items().get(0).quantity().intValue());
        assertTrue(result.overallConfidence() >= 0.80);
    }

    @Test
    void parsesIndianRestaurantReceiptWithoutPromotingMetadataToItems() {
        ReceiptOcrResult result = ReceiptOcrParser.parse("""
                MURUGAN IDLI SHOP
                NANGANALLUR, CHENNAI
                TEL: 9876543210
                GSTIN: 33ABCDE1234F1Z5
                ITEM       QTY RATE AMOUNT
                GHEE PONAL 1 50 50
                COFFEE 1 25 25
                Ticket Total 75.00
                CGST 9% = 6.75
                SGST 9% = 6.75
                Total 88.50
                """);

        assertEquals("MURUGAN IDLI SHOP", result.merchantName());
        assertEquals(new BigDecimal("75.00"), result.subtotalAmount());
        assertEquals(new BigDecimal("6.75"), result.cgstAmount());
        assertEquals(new BigDecimal("6.75"), result.sgstAmount());
        assertEquals(new BigDecimal("13.50"), result.taxAmount());
        assertEquals(new BigDecimal("88.50"), result.totalAmount());
        assertEquals("Food", result.suggestedCategory());
        assertEquals(2, result.items().size());
        assertEquals("GHEE PONAL", result.items().get(0).itemName());
        assertEquals(new BigDecimal("50.00"), result.items().get(0).lineTotal());
        assertEquals("COFFEE", result.items().get(1).itemName());
        assertEquals(new BigDecimal("25.00"), result.items().get(1).lineTotal());
        assertEquals(Boolean.TRUE, result.financiallyConsistent());
    }

    @Test
    void ignoresPhoneAddressAndTaxIdentifiersAsItems() {
        ReceiptOcrResult result = ReceiptOcrParser.parse("""
                STORE NAME
                ADDRESS: 12 MAIN ROAD, CHENNAI
                TIN NO 7
                TEL 044 12345678
                GSTIN 33ABCDE1234F1Z5
                Total 44.00
                """);

        assertEquals(0, result.items().size());
        assertEquals(new BigDecimal("44.00"), result.totalAmount());
    }

    @Test
    void parsesCommaAmountsAndIgnoresTaxPercentagesWithoutAmounts() {
        ReceiptOcrResult result = ReceiptOcrParser.parse("Grand Total 1,250.00\nCGST 9%\nSGST 9%");

        assertEquals(new BigDecimal("1250.00"), result.totalAmount());
        assertNull(result.taxAmount());
    }

    @Test
    void parsesIgstAmount() {
        ReceiptOcrResult result = ReceiptOcrParser.parse("Subtotal 100.00\nIGST 18% 18.00\nAmount Payable 118.00");

        assertEquals(new BigDecimal("18.00"), result.igstAmount());
        assertEquals(new BigDecimal("18.00"), result.taxAmount());
        assertEquals(Boolean.TRUE, result.financiallyConsistent());
    }
}
