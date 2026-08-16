package com.financebuddy.backend.dto;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

public enum TransactionSortOption {
    NEWEST,
    OLDEST,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT;

    public static TransactionSortOption from(String value) {
        if (value == null || value.isBlank()) {
            return NEWEST;
        }

        String normalized = value.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');

        return switch (normalized) {
            case "NEWEST", "LATEST" -> NEWEST;
            case "OLDEST" -> OLDEST;
            case "HIGHEST", "HIGHEST_AMOUNT" -> HIGHEST_AMOUNT;
            case "LOWEST", "LOWEST_AMOUNT" -> LOWEST_AMOUNT;
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sort must be NEWEST, OLDEST, HIGHEST_AMOUNT, or LOWEST_AMOUNT."
            );
        };
    }
}
