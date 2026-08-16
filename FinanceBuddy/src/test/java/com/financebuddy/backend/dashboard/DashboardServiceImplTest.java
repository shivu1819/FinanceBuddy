package com.financebuddy.backend.dashboard;

import com.financebuddy.backend.budget.Budget;
import com.financebuddy.backend.budget.BudgetRepository;
import com.financebuddy.backend.budget.BudgetStatus;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;

    @BeforeEach
    void setUpAuthentication() {
        user = User.builder().id(7L).email("dashboard@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null)
        );
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void summaryIncludesAggregatedTotalsAndCurrentBudgetAnalytics() {
        TransactionRepository.TransactionTotalsProjection totals =
                mock(TransactionRepository.TransactionTotalsProjection.class);
        when(totals.getTotalIncome()).thenReturn(new BigDecimal("5000.00"));
        when(totals.getTotalExpense()).thenReturn(new BigDecimal("1400.00"));
        when(transactionRepository.getTransactionTotalsByUserId(user.getId())).thenReturn(totals);
        when(transactionRepository.countTransactionsByUserId(user.getId())).thenReturn(8L);
        when(categoryRepository.countCategoriesByUserId(user.getId())).thenReturn(4L);

        Budget budget = Budget.builder()
                .id(2L)
                .user(user)
                .monthlyLimit(new BigDecimal("1000.00"))
                .budgetMonth(YearMonth.now())
                .status(BudgetStatus.SAFE)
                .build();
        when(budgetRepository.findByUserAndBudgetMonth(user, YearMonth.now()))
                .thenReturn(Optional.of(budget));
        when(transactionRepository.sumAmountByUserIdAndTransactionTypeAndDateBetween(
                user.getId(),
                "EXPENSE",
                YearMonth.now().atDay(1),
                YearMonth.now().atEndOfMonth()
        )).thenReturn(new BigDecimal("1200.00"));

        var response = dashboardService.getDashboardSummary();

        assertEquals(0, response.getTotalIncome().compareTo(new BigDecimal("5000.00")));
        assertEquals(0, response.getTotalExpense().compareTo(new BigDecimal("1400.00")));
        assertEquals(0, response.getTotalBalance().compareTo(new BigDecimal("3600.00")));
        assertEquals(0, response.getTotalSavings().compareTo(new BigDecimal("3600.00")));
        assertEquals(0, response.getRemainingBudget().compareTo(BigDecimal.ZERO));
        assertEquals(120, response.getBudgetUsagePercentage());
        assertEquals(BudgetStatus.EXCEEDED, response.getBudgetStatus());
        assertEquals(8L, response.getTotalTransactions());
        assertEquals(4L, response.getTotalCategories());
        verify(transactionRepository).getTransactionTotalsByUserId(user.getId());
    }
}
