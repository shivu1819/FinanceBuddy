package com.financebuddy.backend.ai;

import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.Transaction;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AIInsightsServiceImplTest {
    @Mock TransactionRepository transactionRepository;
    @Mock UserRepository userRepository;
    private User user;

    @BeforeEach
    void setup() {
        user = User.builder().id(7L).email("insights@example.com").build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @AfterEach
    void cleanup() { SecurityContextHolder.clearContext(); }

    @Test
    void calculatesUserScopedTotalsCategoriesAndTrend() {
        Category food = Category.builder().name("Food").build();
        when(transactionRepository.findByUserId(7L)).thenReturn(List.of(
                transaction("INCOME", "1000", "2026-01-05", food),
                transaction("EXPENSE", "200", "2026-01-06", food),
                transaction("EXPENSE", "100", "2026-02-06", food)
        ));

        AIInsightResponse response = new AIInsightsServiceImpl(transactionRepository, userRepository).getInsights();

        assertEquals(new BigDecimal("1000"), response.totalIncome());
        assertEquals(new BigDecimal("300"), response.totalExpenses());
        assertEquals(new BigDecimal("700"), response.netSavings());
        assertEquals(new BigDecimal("70.00"), response.savingsPercentage());
        assertEquals("Food", response.highestSpendingCategory());
        assertEquals(2, response.monthlyTrend().size());
        assertEquals("Insights generated from your recorded transactions.", response.message());
    }

    @Test
    void returnsSafeEmptyResponseWhenUserHasNoTransactions() {
        when(transactionRepository.findByUserId(7L)).thenReturn(List.of());

        AIInsightResponse response = new AIInsightsServiceImpl(transactionRepository, userRepository).getInsights();

        assertEquals(BigDecimal.ZERO, response.totalIncome());
        assertEquals(BigDecimal.ZERO, response.totalExpenses());
        assertEquals(0, response.healthScore());
        assertEquals(false, response.enoughData());
        assertEquals("Add more transactions to generate meaningful insights.", response.message());
    }

    @Test
    void addsRiskInsightWhenExpensesExceedIncome() {
        Category essentials = Category.builder().name("Essentials").build();
        when(transactionRepository.findByUserId(7L)).thenReturn(List.of(
                transaction("INCOME", "100", "2026-01-05", essentials),
                transaction("EXPENSE", "150", "2026-01-06", essentials)
        ));

        AIInsightResponse response = new AIInsightsServiceImpl(transactionRepository, userRepository).getInsights();

        assertTrue(response.insights().stream().anyMatch(insight -> "Financial Risk/Warning".equals(insight.category())));
    }

    private Transaction transaction(String type, String amount, String date, Category category) {
        return Transaction.builder().user(user).transactionType(type).amount(new BigDecimal(amount))
                .transactionDate(LocalDate.parse(date)).category(category).paymentMethod("Cash").build();
    }
}
