package com.financebuddy.backend.budget;

import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserAndBudgetMonth(User user, YearMonth budgetMonth);

    List<Budget> findAllByUserAndBudgetMonth(User user, YearMonth budgetMonth);

    List<Budget> findByUserOrderByBudgetMonthDesc(User user);

    Optional<Budget> findByUserAndCategoryAndBudgetMonth(User user, Category category, YearMonth budgetMonth);

    Optional<Budget> findByIdAndUser(Long id, User user);
}
