package com.financebuddy.backend.receipt;

public interface ReceiptOcrService {

    ReceiptOcrResult scan(byte[] imageBytes);
}
