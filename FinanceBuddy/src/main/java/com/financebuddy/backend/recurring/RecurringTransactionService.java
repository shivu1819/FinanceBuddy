package com.financebuddy.backend.recurring;

import java.util.List;

public interface RecurringTransactionService {
    List<RecurringTransactionResponse> getAll();
    RecurringTransactionResponse create(RecurringTransactionRequest request);
    RecurringTransactionResponse update(Long id, RecurringTransactionRequest request);
    RecurringTransactionResponse setActive(Long id, boolean active);
    void delete(Long id);
}
