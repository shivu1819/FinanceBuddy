package com.financebuddy.backend.budget;

public interface BudgetService {

    BudgetResponse createBudget(BudgetRequest request);

    BudgetResponse getCurrentBudget();

    BudgetResponse updateBudget(Long id, BudgetRequest request);

    void deleteBudget(Long id);
}
