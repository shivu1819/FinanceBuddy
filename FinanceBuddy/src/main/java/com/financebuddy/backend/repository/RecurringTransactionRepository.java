package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserId(Long userId);

    List<RecurringTransaction> findByUserIdAndActiveTrue(Long userId);

    List<RecurringTransaction> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate runDate);

    List<RecurringTransaction> findByUserIdAndFrequency(Long userId, String frequency);

    boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);

    boolean existsByBankAccountIdAndUserId(Long bankAccountId, Long userId);
}
