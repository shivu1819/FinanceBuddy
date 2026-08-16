package com.financebuddy.backend.budget;

import com.financebuddy.backend.entity.User;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User user;

    @BeforeEach
    void setUpAuthentication() {
        user = User.builder().id(12L).email("budget@example.com").build();
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
    void currentBudgetIncludesExpensesThatAlreadyExistForTheMonth() {
        YearMonth month = YearMonth.now();
        Budget budget = Budget.builder()
                .id(3L)
                .user(user)
                .monthlyLimit(new BigDecimal("1000.00"))
                .budgetMonth(month)
                .status(BudgetStatus.SAFE)
                .build();
        when(budgetRepository.findByUserAndBudgetMonth(user, month)).thenReturn(Optional.of(budget));
        when(transactionRepository.sumAmountByUserIdAndTransactionTypeAndDateBetween(
                user.getId(),
                "EXPENSE",
                month.atDay(1),
                month.atEndOfMonth()
        )).thenReturn(new BigDecimal("850.00"));

        BudgetResponse response = budgetService.getCurrentBudget();

        assertEquals(0, response.getSpent().compareTo(new BigDecimal("850.00")));
        assertEquals(0, response.getRemaining().compareTo(new BigDecimal("150.00")));
        assertEquals(85, response.getPercentageUsed());
        assertEquals(BudgetStatus.WARNING, response.getStatus());
    }
}
