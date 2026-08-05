package com.financebuddy.backend.repository;

import com.financebuddy.backend.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Deprecated(since = "B-FIX-1.2", forRemoval = false)
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUserId(Long userId);

    List<SavingsGoal> findByUserIdAndStatus(Long userId, String status);
}
