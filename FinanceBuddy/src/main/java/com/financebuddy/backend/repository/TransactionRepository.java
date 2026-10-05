package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.user.id = :userId
            ORDER BY t.transactionDate DESC, t.createdAt DESC, t.id DESC
            """)
    List<Transaction> findByUserId(Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndCategory(Long userId, Category category);

    @EntityGraph(attributePaths = "category")
    List<Transaction> findByUserIdAndCategoryId(Long userId, Long categoryId);

    @EntityGraph(attributePaths = "category")
    List<Transaction> findByUserIdAndTransactionType(Long userId, String transactionType);

    List<Transaction> findByUserIdAndBankAccountId(Long userId, Long bankAccountId);

    boolean existsByCategoryIdAndUserId(Long categoryId, Long userId);

    boolean existsByBankAccountIdAndUserId(Long bankAccountId, Long userId);

    @EntityGraph(attributePaths = "category")
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

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
            SELECT
                COALESCE(SUM(CASE WHEN t.transactionType = 'INCOME' THEN t.amount ELSE 0 END), 0)
                    AS totalIncome,
                COALESCE(SUM(CASE WHEN t.transactionType = 'EXPENSE' THEN t.amount ELSE 0 END), 0)
                    AS totalExpense
            FROM Transaction t
            WHERE t.user.id = :userId
            """)
    TransactionTotalsProjection getTransactionTotalsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.user.id = :userId
              AND t.transactionType = :transactionType
              AND t.transactionDate BETWEEN :startDate AND :endDate
            """)
    BigDecimal sumAmountByUserIdAndTransactionTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("transactionType") String transactionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.category.id = :categoryId AND t.transactionType = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumExpenseByUserIdAndCategoryIdAndDateBetween(@Param("userId") Long userId, @Param("categoryId") Long categoryId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("""
            SELECT
                c.name AS categoryName,
                c.color AS categoryColor,
                c.icon AS categoryIcon,
                SUM(t.amount) AS totalAmount
            FROM Transaction t
            JOIN t.category c
            WHERE t.user.id = :userId
              AND t.transactionType = 'EXPENSE'
            GROUP BY c.id, c.name, c.color, c.icon
            ORDER BY SUM(t.amount) DESC, c.name ASC
            """)
    List<CategoryExpenseProjection> getCategoryExpensesByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT
                YEAR(t.transactionDate) AS yearValue,
                MONTH(t.transactionDate) AS monthValue,
                COALESCE(SUM(CASE WHEN t.transactionType = 'INCOME' THEN t.amount ELSE 0 END), 0)
                    AS income,
                COALESCE(SUM(CASE WHEN t.transactionType = 'EXPENSE' THEN t.amount ELSE 0 END), 0)
                    AS expense
            FROM Transaction t
            WHERE t.user.id = :userId
            GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate)
            ORDER BY YEAR(t.transactionDate), MONTH(t.transactionDate)
            """)
    List<MonthlyCashFlowProjection> getMonthlyCashFlowByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT t
            FROM Transaction t
            JOIN FETCH t.category
            WHERE t.user.id = :userId
            ORDER BY t.transactionDate DESC, t.createdAt DESC, t.id DESC
            """)
    List<Transaction> findLatestTransactionsByUserId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "category")
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.user.id = :userId
              AND (:searchTerm IS NULL
                   OR LOWER(COALESCE(t.description, '')) LIKE :searchTerm)
              AND (:titleTerm IS NULL
                   OR LOWER(COALESCE(t.description, '')) LIKE :titleTerm)
              AND (:notesTerm IS NULL
                   OR LOWER(COALESCE(t.description, '')) LIKE :notesTerm)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:transactionType IS NULL OR t.transactionType = :transactionType)
              AND (:startDate IS NULL OR t.transactionDate >= :startDate)
              AND (:endDate IS NULL OR t.transactionDate <= :endDate)
              AND (:minimumAmount IS NULL OR t.amount >= :minimumAmount)
              AND (:maximumAmount IS NULL OR t.amount <= :maximumAmount)
            """)
    Page<Transaction> searchTransactions(
            @Param("userId") Long userId,
            @Param("searchTerm") String searchTerm,
            @Param("titleTerm") String titleTerm,
            @Param("notesTerm") String notesTerm,
            @Param("categoryId") Long categoryId,
            @Param("transactionType") String transactionType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minimumAmount") BigDecimal minimumAmount,
            @Param("maximumAmount") BigDecimal maximumAmount,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.user.id = :userId
            """)
    long countTransactionsByUserId(@Param("userId") Long userId);

    interface TransactionTotalsProjection {

        BigDecimal getTotalIncome();

        BigDecimal getTotalExpense();
    }

    interface CategoryExpenseProjection {

        String getCategoryName();

        String getCategoryColor();

        String getCategoryIcon();

        BigDecimal getTotalAmount();
    }

    interface MonthlyCashFlowProjection {

        Integer getYearValue();

        Integer getMonthValue();

        BigDecimal getIncome();

        BigDecimal getExpense();
    }
}
