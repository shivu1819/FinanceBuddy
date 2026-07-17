package com.financebuddy.backend.goal;

import java.util.List;

public interface GoalService {

    GoalResponse createGoal(GoalRequest request);

    List<GoalResponse> getGoals();

    GoalResponse getGoalById(Long id);

    GoalResponse updateGoal(Long id, GoalRequest request);

    GoalResponse addMoney(Long id, GoalSavingsRequest request);

    void deleteGoal(Long id);
}
