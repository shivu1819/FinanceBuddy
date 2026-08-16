package com.financebuddy.backend.dashboard;

import com.financebuddy.backend.dto.DashboardCategoryExpenseResponse;
import com.financebuddy.backend.dto.DashboardMonthlyChartResponse;
import com.financebuddy.backend.dto.DashboardRecentTransactionResponse;
import com.financebuddy.backend.dto.DashboardSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/category-expenses")
    public ResponseEntity<List<DashboardCategoryExpenseResponse>> getCategoryExpenses() {
        return ResponseEntity.ok(dashboardService.getCategoryExpenses());
    }

    @GetMapping("/monthly-chart")
    public ResponseEntity<List<DashboardMonthlyChartResponse>> getMonthlyChart() {
        return ResponseEntity.ok(dashboardService.getMonthlyChart());
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<List<DashboardRecentTransactionResponse>> getRecentTransactions() {
        return ResponseEntity.ok(dashboardService.getRecentTransactions());
    }
}
