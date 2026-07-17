package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {

    List<MonthlyBudget> findByUserId(Long userId);

    List<MonthlyBudget> findByUserIdAndBudgetMonthAndBudgetYear(Long userId, Integer budgetMonth, Integer budgetYear);

    Optional<MonthlyBudget> findByUserIdAndCategoryIdAndBudgetMonthAndBudgetYear(
            Long userId,
            Long categoryId,
            Integer budgetMonth,
            Integer budgetYear
    );
}
