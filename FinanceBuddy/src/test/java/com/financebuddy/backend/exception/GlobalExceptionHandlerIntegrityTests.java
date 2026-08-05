package com.financebuddy.backend.exception;

import com.financebuddy.backend.dto.ErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.sql.SQLException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerIntegrityTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void bankAccountForeignKeyConflictReturnsSafeConflictResponse() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
                "Persistence failure",
                new SQLException("fk_transactions_user_bank_account internal database detail")
        );

        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(
                exception,
                request("DELETE", "/api/bank-accounts/42")
        );

        assertEquals(409, response.getStatusCode().value());
        assertEquals(
                "Bank account cannot be deleted because it is used by existing transactions.",
                response.getBody().message()
        );
        assertFalse(response.getBody().message().contains("fk_transactions"));
    }

    @Test
    void beanConstraintViolationReturnsBadRequestWithoutInternalDetails() {
        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(
                new ConstraintViolationException("Internal validation detail", Set.of()),
                request("POST", "/api/transactions")
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Validation failed.", response.getBody().message());
    }

    @Test
    void entityNotFoundReturnsSafeNotFoundResponse() {
        ResponseEntity<ErrorResponse> response = handler.handleEntityNotFound(
                new EntityNotFoundException("Internal entity and identifier"),
                request("GET", "/api/goals/42")
        );

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Resource not found.", response.getBody().message());
    }

    private MockHttpServletRequest request(String method, String path) {
        return new MockHttpServletRequest(method, path);
    }
}
