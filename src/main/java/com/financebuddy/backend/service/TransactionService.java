package com.financebuddy.backend.service;

import com.financebuddy.backend.dto.TransactionRequest;
import com.financebuddy.backend.dto.TransactionResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionRequest request);

    List<TransactionResponse> getTransactions();

    TransactionResponse getTransactionById(Long id);

    TransactionResponse updateTransaction(Long id, TransactionRequest request);

    void deleteTransaction(Long id);

    List<TransactionResponse> getTransactionsByType(String transactionType);

    List<TransactionResponse> getTransactionsByCategory(Long categoryId);
}
