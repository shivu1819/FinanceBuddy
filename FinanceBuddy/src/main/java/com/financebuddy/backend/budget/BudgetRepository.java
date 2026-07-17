package com.financebuddy.backend.budget;

import com.financebuddy.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserAndBudgetMonth(User user, YearMonth budgetMonth);

    Optional<Budget> findByIdAndUser(Long id, User user);
}
