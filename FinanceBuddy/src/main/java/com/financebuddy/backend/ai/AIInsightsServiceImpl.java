package com.financebuddy.backend.ai;

import com.financebuddy.backend.entity.Transaction;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.budget.Budget;
import com.financebuddy.backend.budget.BudgetRepository;
import com.financebuddy.backend.goal.Goal;
import com.financebuddy.backend.goal.GoalRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIInsightsServiceImpl implements AIInsightsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final GoalRepository goalRepository;

    @Autowired
    public AIInsightsServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository,
                                 BudgetRepository budgetRepository, GoalRepository goalRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.budgetRepository = budgetRepository;
        this.goalRepository = goalRepository;
    }

    public AIInsightsServiceImpl(TransactionRepository transactionRepository, UserRepository userRepository) {
        this(transactionRepository, userRepository, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public AIInsightResponse getInsights() {
        User user = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());
        BigDecimal income = sum(transactions, "INCOME");
        BigDecimal expenses = sum(transactions, "EXPENSE");
        BigDecimal savings = income.subtract(expenses);
        BigDecimal savingsPercentage = income.signum() == 0 ? BigDecimal.ZERO
                : savings.multiply(BigDecimal.valueOf(100)).divide(income, 2, RoundingMode.HALF_UP);

        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal[]> monthly = new LinkedHashMap<>();
        for (Transaction transaction : transactions) {
            YearMonth month = YearMonth.from(transaction.getTransactionDate());
            BigDecimal[] totals = monthly.computeIfAbsent(month, ignored -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            boolean isIncome = "INCOME".equals(transaction.getTransactionType());
            totals[isIncome ? 0 : 1] = totals[isIncome ? 0 : 1].add(transaction.getAmount());
            if (!isIncome) {
                String category = transaction.getCategory() == null ? "Uncategorized" : transaction.getCategory().getName();
                categoryTotals.merge(category, transaction.getAmount(), BigDecimal::add);
            }
        }

        List<Map.Entry<String, BigDecimal>> ranked = categoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .toList();
        List<SpendingInsight> top = ranked.stream().limit(5)
                .map(entry -> new SpendingInsight(entry.getKey(), entry.getValue(), percentage(entry.getValue(), expenses)))
                .toList();
        List<MonthlyInsight> trend = monthly.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(entry -> new MonthlyInsight(entry.getKey().toString(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[0].subtract(entry.getValue()[1])))
                .toList();

        List<String> recommendations = new ArrayList<>();
        if (!ranked.isEmpty()) recommendations.add(ranked.get(0).getKey() + " is your highest spending category.");
        if (savingsPercentage.compareTo(BigDecimal.TEN) < 0) recommendations.add("Your savings rate is low; review flexible expenses.");
        else recommendations.add("Your savings rate is healthy; keep your current saving habit.");
        if (trend.size() >= 2 && trend.get(trend.size() - 1).expenses().compareTo(trend.get(trend.size() - 2).expenses()) > 0) {
            recommendations.add("Your expenses increased compared with the previous month.");
        }

        YearMonth currentMonth = YearMonth.now();
        BigDecimal budgetLimit = BigDecimal.ZERO;
        BigDecimal budgetSpent = BigDecimal.ZERO;
        if (budgetRepository != null) {
            for (Budget budget : budgetRepository.findAllByUserAndBudgetMonth(user, currentMonth)) {
                budgetLimit = budgetLimit.add(budget.getMonthlyLimit());
                budgetSpent = budgetSpent.add(transactions.stream()
                        .filter(t -> "EXPENSE".equals(t.getTransactionType())
                                && !t.getTransactionDate().isBefore(currentMonth.atDay(1))
                                && !t.getTransactionDate().isAfter(currentMonth.atEndOfMonth())
                                && (budget.getCategory() == null || (t.getCategory() != null
                                && budget.getCategory().getId().equals(t.getCategory().getId()))))
                        .map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
            }
        }
        int budgetUtilization = budgetLimit.signum() == 0 ? 0 : budgetSpent.multiply(BigDecimal.valueOf(100))
                .divide(budgetLimit, 0, RoundingMode.HALF_UP).intValue();

        List<FinancialInsight> insights = new ArrayList<>();
        if (!ranked.isEmpty()) {
            insights.add(new FinancialInsight("Spending Analysis", "Highest spending category",
                    ranked.get(0).getKey() + " accounts for your largest share of expenses.", "LOW",
                    "Review recent " + ranked.get(0).getKey() + " transactions for avoidable costs."));
        }
        if (budgetLimit.signum() > 0 && budgetUtilization >= 70) {
            String severity = budgetUtilization >= 100 ? "HIGH" : "MEDIUM";
            insights.add(new FinancialInsight("Budget Warning", "Budget needs attention",
                    "You have used " + budgetUtilization + "% of this month's budget.", severity,
                    "Pause non-essential spending and monitor the rest of the month."));
            recommendations.add("Your current budget is " + budgetUtilization + "% used.");
        }
        if (income.signum() > 0) {
            insights.add(new FinancialInsight("Income/Expense Summary", "Cash flow summary",
                    "You recorded " + income + " income and " + expenses + " in expenses.", "LOW",
                    savings.signum() >= 0 ? "Keep directing part of your surplus to savings." : "Prioritize reducing expenses before taking on new commitments."));
        }
        if (expenses.compareTo(income) > 0) {
            insights.add(new FinancialInsight("Financial Risk/Warning", "Expenses exceed income",
                    "Your recorded expenses are higher than your recorded income.", "HIGH",
                    "Reduce non-essential spending and review recurring commitments."));
        }
        if (savingsPercentage.compareTo(BigDecimal.TEN) < 0) {
            insights.add(new FinancialInsight("Savings Suggestion", "Build your savings rate",
                    "Your current savings rate is " + savingsPercentage + "%.", "MEDIUM",
                    "Set aside a fixed amount on payday and review flexible expenses."));
        } else if (income.signum() > 0) {
            insights.add(new FinancialInsight("Savings Recommendation", "Savings are on track",
                    "Your current savings rate is " + savingsPercentage + "%.", "LOW",
                    "Keep this habit and consider directing surplus toward a savings goal."));
        }

        List<GoalProgressInsight> goalProgress = new ArrayList<>();
        if (goalRepository != null) {
            for (Goal goal : goalRepository.findByUserOrderByCreatedAtDesc(user)) {
                int progress = goal.getTargetAmount().signum() == 0 ? 0 : goal.getSavedAmount()
                        .multiply(BigDecimal.valueOf(100)).divide(goal.getTargetAmount(), 0, RoundingMode.HALF_UP).intValue();
                goalProgress.add(new GoalProgressInsight(goal.getTitle(), goal.getTargetAmount(), goal.getSavedAmount(),
                        Math.min(progress, 100), goal.getStatus().name()));
                if (goal.getTargetDate() != null && goal.getCreatedAt() != null
                        && goal.getTargetDate().isAfter(LocalDate.now())) {
                    long totalDays = Math.max(1, ChronoUnit.DAYS.between(goal.getCreatedAt().toLocalDate(), goal.getTargetDate()));
                    long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(goal.getCreatedAt().toLocalDate(), LocalDate.now()));
                    int expectedProgress = (int) Math.min(100, elapsedDays * 100 / totalDays);
                    if (progress + 10 < expectedProgress) {
                        insights.add(new FinancialInsight("Goal Progress", goal.getTitle() + " is behind schedule",
                                "This goal is " + progress + "% funded; approximately " + expectedProgress + "% would be expected by now.", "MEDIUM",
                                "Increase contributions or review the target date."));
                    }
                }
            }
        }
        if (!goalProgress.isEmpty()) {
            GoalProgressInsight goal = goalProgress.get(0);
            insights.add(new FinancialInsight("Goal Progress", goal.title(),
                    goal.progressPercentage() + "% of this goal is funded.", "LOW",
                    "Continue regular contributions to stay on track."));
        }
        if (transactions.isEmpty() && goalProgress.isEmpty() && budgetLimit.signum() == 0) {
            insights.add(new FinancialInsight("Financial Health", "Start with a baseline",
                    "Add income, expense, budget, or goal data to receive personalized insights.", "LOW",
                    "Record your first transaction to begin tracking your financial health."));
        }

        int healthScore = calculateHealthScore(savingsPercentage, budgetUtilization, income, expenses);

        boolean enoughData = transactions.size() >= 2;
        return new AIInsightResponse(income, expenses, savings, savingsPercentage,
                ranked.isEmpty() ? null : ranked.get(0).getKey(), top, trend, recommendations,
                enoughData, enoughData ? "Insights generated from your recorded transactions." : "Add more transactions to generate meaningful insights.",
                healthScore, budgetLimit, budgetSpent, budgetUtilization, insights, goalProgress);
    }

    private int calculateHealthScore(BigDecimal savingsPercentage, int budgetUtilization,
                                     BigDecimal income, BigDecimal expenses) {
        if (income.signum() == 0 && expenses.signum() == 0) return 0;
        int score = 50 + savingsPercentage.intValue() / 2;
        if (budgetUtilization > 70) score -= Math.min(30, budgetUtilization - 70);
        if (expenses.compareTo(income) > 0) score -= 20;
        return Math.max(0, Math.min(100, score));
    }

    private BigDecimal sum(List<Transaction> transactions, String type) {
        return transactions.stream().filter(t -> type.equals(t.getTransactionType())).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percentage(BigDecimal part, BigDecimal whole) {
        return whole.signum() == 0 ? BigDecimal.ZERO : part.multiply(BigDecimal.valueOf(100)).divide(whole, 2, RoundingMode.HALF_UP);
    }

    private User getCurrentUser() {
        if (SecurityContextHolder.getContext().getAuthentication() == null
                || SecurityContextHolder.getContext().getAuthentication().getName() == null
                || "anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found."));
    }
}
