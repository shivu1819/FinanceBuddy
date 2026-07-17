package com.financebuddy.backend.goal;

import com.financebuddy.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserOrderByCreatedAtDesc(User user);

    Optional<Goal> findByIdAndUser(Long id, User user);
}
