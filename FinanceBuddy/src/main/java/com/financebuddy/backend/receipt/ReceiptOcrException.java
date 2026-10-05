package com.financebuddy.backend.receipt;

public class ReceiptOcrException extends RuntimeException {

    public ReceiptOcrException(String message) {
        super(message);
    }

    public ReceiptOcrException(String message, Throwable cause) {
        super(message, cause);
    }
}
