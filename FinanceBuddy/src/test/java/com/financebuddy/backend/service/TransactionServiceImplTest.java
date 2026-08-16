package com.financebuddy.backend.service;

import com.financebuddy.backend.dto.TransactionPageResponse;
import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.Transaction;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.BankAccountRepository;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import com.financebuddy.backend.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;

    @BeforeEach
    void setUpAuthentication() {
        user = User.builder().id(4L).email("transactions@example.com").build();
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
    void filteringAndPaginationArePassedToOneRepositoryQuery() {
        Category category = Category.builder()
                .id(9L)
                .user(user)
                .name("Food")
                .type("EXPENSE")
                .color("#123456")
                .icon("utensils")
                .build();
        Transaction transaction = Transaction.builder()
                .id(21L)
                .user(user)
                .category(category)
                .description("Morning coffee memo")
                .amount(new BigDecimal("150.00"))
                .transactionType("EXPENSE")
                .paymentMethod("CARD")
                .transactionDate(LocalDate.of(2026, 7, 12))
                .build();
        PageImpl<Transaction> repositoryPage = new PageImpl<>(
                List.of(transaction),
                PageRequest.of(1, 2),
                5
        );
        when(transactionRepository.searchTransactions(
                eq(user.getId()),
                eq("%coffee%"),
                eq("%morning%"),
                eq("%memo%"),
                eq(category.getId()),
                eq("EXPENSE"),
                eq(LocalDate.of(2026, 7, 1)),
                eq(LocalDate.of(2026, 7, 31)),
                eq(new BigDecimal("100.00")),
                eq(new BigDecimal("500.00")),
                any(Pageable.class)
        )).thenReturn(repositoryPage);

        TransactionPageResponse response = transactionService.searchTransactions(
                " Coffee ",
                "Morning",
                "memo",
                category.getId(),
                "expense",
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 31),
                new BigDecimal("100.00"),
                new BigDecimal("500.00"),
                "Highest Amount",
                1,
                2
        );

        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertEquals(1, response.getContent().size());
        assertEquals("Morning coffee memo", response.getContent().getFirst().getDescription());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(transactionRepository).searchTransactions(
                eq(user.getId()),
                eq("%coffee%"),
                eq("%morning%"),
                eq("%memo%"),
                eq(category.getId()),
                eq("EXPENSE"),
                eq(LocalDate.of(2026, 7, 1)),
                eq(LocalDate.of(2026, 7, 31)),
                eq(new BigDecimal("100.00")),
                eq(new BigDecimal("500.00")),
                pageable.capture()
        );
        assertEquals("amount", pageable.getValue().getSort().iterator().next().getProperty());
    }
}
