package com.financebuddy.backend.integrity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financebuddy.backend.entity.BankAccount;
import com.financebuddy.backend.entity.Category;
import com.financebuddy.backend.entity.Transaction;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.BankAccountRepository;
import com.financebuddy.backend.repository.CategoryRepository;
import com.financebuddy.backend.repository.TransactionRepository;
import com.financebuddy.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DatabaseIntegrityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void categoryDeleteIsBlockedWhenTransactionsExist() throws Exception {
        AuthenticatedUser owner = registerAndLogin("category-delete");
        long categoryId = createCategory(owner.token(), "Groceries", "EXPENSE");

        createTransaction(owner.token(), categoryId, null);
        assertTrue(transactionRepository.existsByCategoryIdAndUserId(categoryId, owner.user().getId()));

        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Category cannot be deleted because it is used by existing transactions."
                ));
    }

    @Test
    void categoryDuplicateIsBlockedCaseInsensitively() throws Exception {
        AuthenticatedUser owner = registerAndLogin("category-duplicate");

        mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(categoryBody("Food", "EXPENSE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Food"));

        mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(categoryBody(" food ", "EXPENSE"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Category already exists"));
    }

    @Test
    void sameNormalizedCategoryNameIsAllowedForDifferentUsers() throws Exception {
        AuthenticatedUser firstUser = registerAndLogin("category-owner-one");
        AuthenticatedUser secondUser = registerAndLogin("category-owner-two");

        createCategory(firstUser.token(), "Travel", "EXPENSE");

        mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(secondUser.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(categoryBody(" TRAVEL ", "EXPENSE"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("TRAVEL"));
    }

    @Test
    void duplicateBudgetMonthIsBlocked() throws Exception {
        AuthenticatedUser owner = registerAndLogin("budget-duplicate");
        String budgetMonth = YearMonth.now().plusYears(2).toString();
        Map<String, Object> request = Map.of(
                "monthlyLimit", 25000,
                "budgetMonth", budgetMonth
        );

        mockMvc.perform(post("/api/budgets")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/budgets")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Budget already exists for this month."));
    }

    @Test
    void bankAccountDeleteIsBlockedByDatabaseWhenTransactionsExist() throws Exception {
        AuthenticatedUser owner = registerAndLogin("bank-delete");
        Category category = categoryRepository.saveAndFlush(Category.builder()
                .user(owner.user())
                .name("Bank-linked expense")
                .type("EXPENSE")
                .systemDefault(false)
                .build());
        BankAccount bankAccount = createBankAccount(owner.user(), "Primary account");

        transactionRepository.saveAndFlush(Transaction.builder()
                .user(owner.user())
                .category(category)
                .bankAccount(bankAccount)
                .transactionType("EXPENSE")
                .amount(BigDecimal.TEN)
                .description("Deletion protection test")
                .paymentMethod("BANK")
                .transactionDate(LocalDate.now())
                .build());

        assertTrue(transactionRepository.existsByBankAccountIdAndUserId(
                bankAccount.getId(),
                owner.user().getId()
        ));

        Long bankAccountId = bankAccount.getId();
        entityManager.clear();
        BankAccount bankAccountToDelete = bankAccountRepository.findById(bankAccountId).orElseThrow();

        assertThrows(DataIntegrityViolationException.class, () -> {
            bankAccountRepository.delete(bankAccountToDelete);
            bankAccountRepository.flush();
        });
    }

    @Test
    void crossUserCategoryLinkageIsRejectedWithoutRevealingOwnership() throws Exception {
        AuthenticatedUser categoryOwner = registerAndLogin("category-link-owner");
        AuthenticatedUser otherUser = registerAndLogin("category-link-other");
        long foreignCategoryId = createCategory(categoryOwner.token(), "Private category", "EXPENSE");

        mockMvc.perform(post("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherUser.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(transactionBody(foreignCategoryId, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test
    void crossUserBankAccountLinkageIsRejectedWithoutRevealingOwnership() throws Exception {
        AuthenticatedUser bankOwner = registerAndLogin("bank-link-owner");
        AuthenticatedUser otherUser = registerAndLogin("bank-link-other");
        BankAccount foreignBankAccount = createBankAccount(bankOwner.user(), "Private bank account");
        long ownedCategoryId = createCategory(otherUser.token(), "Owned category", "EXPENSE");

        mockMvc.perform(post("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherUser.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(transactionBody(
                                ownedCategoryId,
                                foreignBankAccount.getId()
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bank account not found"));
    }

    private AuthenticatedUser registerAndLogin(String label) throws Exception {
        String email = label + "-" + UUID.randomUUID() + "@example.com";
        String password = "Integrity-" + UUID.randomUUID() + "7";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "fullName", "Database Integrity Test",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
        User user = userRepository.findByEmail(email).orElseThrow();
        return new AuthenticatedUser(token, user);
    }

    private long createCategory(String token, String name, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(categoryBody(name, type))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void createTransaction(String token, long categoryId, Long bankAccountId) throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(transactionBody(categoryId, bankAccountId))))
                .andExpect(status().isCreated());
    }

    private BankAccount createBankAccount(User owner, String accountName) {
        return bankAccountRepository.saveAndFlush(BankAccount.builder()
                .user(owner)
                .accountName(accountName)
                .accountType("CHECKING")
                .bankName("Integrity Test Bank")
                .currentBalance(BigDecimal.valueOf(1000))
                .active(true)
                .build());
    }

    private Map<String, Object> categoryBody(String name, String type) {
        return Map.of("name", name, "type", type);
    }

    private Map<String, Object> transactionBody(long categoryId, Long bankAccountId) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", 125.50);
        body.put("description", "Database integrity test");
        body.put("transactionDate", LocalDate.now().toString());
        body.put("paymentMethod", "BANK");
        body.put("transactionType", "EXPENSE");
        body.put("categoryId", categoryId);
        if (bankAccountId != null) {
            body.put("bankAccountId", bankAccountId);
        }
        return body;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record AuthenticatedUser(String token, User user) {
    }
}
