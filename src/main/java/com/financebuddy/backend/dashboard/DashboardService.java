package com.financebuddy.backend.dashboard;

import com.financebuddy.backend.dto.DashboardCategoryExpenseResponse;
import com.financebuddy.backend.dto.DashboardRecentTransactionResponse;
import com.financebuddy.backend.dto.DashboardSummaryResponse;

import java.util.List;

public interface DashboardService {

    DashboardSummaryResponse getDashboardSummary();

    DashboardSummaryResponse getSummary();

    List<DashboardRecentTransactionResponse> getRecentTransactions();

    List<DashboardCategoryExpenseResponse> getCategoryExpenses();
}
