package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.Transaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndCategory(Long userId, Category category);

    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    List<Transaction> findByUserIdAndTransactionType(Long userId, String transactionType);

    List<Transaction> findByUserIdAndBankAccountId(Long userId, Long bankAccountId);

    boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);

    boolean existsByBankAccountIdAndUserId(Long bankAccountId, Long userId);

    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            WHERE t.user.id = :userId
              AND t.transactionType = 'INCOME'
            """)
    Optional<BigDecimal> getTotalIncomeByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT SUM(t.amount)
            FROM Transaction t
            WHERE t.user.id = :userId
              AND t.transactionType = 'EXPENSE'
            """)
    Optional<BigDecimal> getTotalExpenseByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.user.id = :userId
            ORDER BY t.transactionDate DESC
            """)
    List<Transaction> findLatestTransactionsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.user.id = :userId
            """)
    long countTransactionsByUserId(@Param("userId") Long userId);
}
