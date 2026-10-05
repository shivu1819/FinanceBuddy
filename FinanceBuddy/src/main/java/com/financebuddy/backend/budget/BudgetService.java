package com.financebuddy.backend.budget;

public interface BudgetService {

    BudgetResponse createBudget(BudgetRequest request);

    BudgetResponse getCurrentBudget();

    java.util.List<BudgetResponse> getBudgets();

    BudgetResponse updateBudget(Long id, BudgetRequest request);

    void deleteBudget(Long id);
}
