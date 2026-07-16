package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndCategory(Long userId, Category category);

    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Transaction> findByUserIdAndTransactionType(Long userId, String transactionType);

    List<Transaction> findByUserIdAndBankAccountId(Long userId, Long bankAccountId);
}
