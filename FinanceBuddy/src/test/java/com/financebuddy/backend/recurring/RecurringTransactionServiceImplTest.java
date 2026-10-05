package com.financebuddy.backend.recurring;

import com.financebuddy.backend.entity.BankAccount;
import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceImplTest {
    @Mock RecurringTransactionRepository repository;
    @Mock UserRepository users;
    @Mock CategoryRepository categories;
    @Mock BankAccountRepository accounts;
    private User user;
    @BeforeEach void setUp(){user=User.builder().id(4L).email("recurring@example.com").build();SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(),null));when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));}
    @AfterEach void tearDown(){SecurityContextHolder.clearContext();}
    @Test void createsOwnedRecurringExpenseWithNextRunDate(){Category category=Category.builder().id(8L).name("Rent").type("EXPENSE").build();when(categories.findByIdAndUserId(8L,4L)).thenReturn(Optional.of(category));when(accounts.findByUserIdAndActiveTrue(4L)).thenReturn(java.util.List.of(BankAccount.builder().id(3L).user(user).active(true).build()));when(repository.save(any())).thenAnswer(invocation->invocation.getArgument(0));RecurringTransactionResponse response=new RecurringTransactionServiceImpl(repository,users,categories,accounts).create(RecurringTransactionRequest.builder().amount(new BigDecimal("100.00")).transactionType("EXPENSE").categoryId(8L).frequency("MONTHLY").startDate(LocalDate.of(2026,9,1)).build());assertEquals(new BigDecimal("100.00"),response.getAmount());assertEquals(LocalDate.of(2026,9,1),response.getNextRunDate());assertEquals(4L,user.getId());}
}
