package com.financebuddy.backend.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CoreBusinessLogicIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void dashboardAndBudgetUseExistingTransactions() throws Exception {
        String token = registerAndLogin("dashboard-core");
        long incomeCategory = createCategory(token, "Salary", "INCOME", "#10b981", "wallet");
        long expenseCategory = createCategory(token, "Food", "EXPENSE", "#ef4444", "utensils");

        createTransaction(token, incomeCategory, "Monthly salary", 5000, "INCOME", LocalDate.now());
        createTransaction(token, expenseCategory, "Groceries", 1000, "EXPENSE", LocalDate.now());
        createTransaction(token, expenseCategory, "Dinner", 250, "EXPENSE", LocalDate.now());

        mockMvc.perform(post("/api/budgets")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "monthlyLimit", 1000,
                                "budgetMonth", YearMonth.now().toString()
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.spent").value(1250))
                .andExpect(jsonPath("$.remaining").value(0))
                .andExpect(jsonPath("$.percentageUsed").value(125))
                .andExpect(jsonPath("$.status").value("EXCEEDED"));

        mockMvc.perform(get("/api/dashboard/summary")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000))
                .andExpect(jsonPath("$.totalExpense").value(1250))
                .andExpect(jsonPath("$.totalBalance").value(3750))
                .andExpect(jsonPath("$.totalSavings").value(3750))
                .andExpect(jsonPath("$.monthlyBudget").value(1000))
                .andExpect(jsonPath("$.spentBudget").value(1250))
                .andExpect(jsonPath("$.remainingBudget").value(0))
                .andExpect(jsonPath("$.budgetUsagePercentage").value(125))
                .andExpect(jsonPath("$.budgetStatus").value("EXCEEDED"));

        mockMvc.perform(get("/api/dashboard/category-expenses")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].categoryColor").value("#ef4444"))
                .andExpect(jsonPath("$[0].categoryIcon").value("utensils"))
                .andExpect(jsonPath("$[0].totalAmount").value(1250))
                .andExpect(jsonPath("$[0].percentage").value(100.0));

        mockMvc.perform(get("/api/dashboard/monthly-chart")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].month").value(YearMonth.now().toString()))
                .andExpect(jsonPath("$[0].income").value(5000))
                .andExpect(jsonPath("$[0].expense").value(1250))
                .andExpect(jsonPath("$[0].savings").value(3750));

        mockMvc.perform(get("/api/dashboard/recent-transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].description").value("Dinner"))
                .andExpect(jsonPath("$[0].transactionType").value("EXPENSE"))
                .andExpect(jsonPath("$[0].formattedDate").isNotEmpty())
                .andExpect(jsonPath("$[0].category.name").value("Food"));
    }

    @Test
    void transactionSearchAppliesFiltersSortingAndPagination() throws Exception {
        String token = registerAndLogin("transaction-search");
        long foodCategory = createCategory(token, "Food", "EXPENSE", null, null);
        long travelCategory = createCategory(token, "Travel", "EXPENSE", null, null);
        LocalDate today = LocalDate.now();

        createTransaction(token, foodCategory, "Morning coffee notes", 150, "EXPENSE", today.minusDays(2));
        createTransaction(token, travelCategory, "Hotel", 900, "EXPENSE", today.minusDays(1));
        createTransaction(token, foodCategory, "Lunch", 300, "EXPENSE", today);

        mockMvc.perform(get("/api/transactions/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("search", "coffee")
                        .param("title", "morning")
                        .param("notes", "notes")
                        .param("categoryId", String.valueOf(foodCategory))
                        .param("type", "expense")
                        .param("startDate", today.minusDays(3).toString())
                        .param("endDate", today.toString())
                        .param("minAmount", "100")
                        .param("maxAmount", "200")
                        .param("sort", "Highest Amount")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].description").value("Morning coffee notes"));

        mockMvc.perform(get("/api/transactions/search")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .param("sort", "OLDEST")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].description").value("Lunch"));
    }

    @Test
    void taxValidationReturnsBadRequestForNegativeDuplicateAndInvalidInputs() throws Exception {
        String token = registerAndLogin("tax-validation");

        mockMvc.perform(post("/api/tax/calculate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"annualIncome\":-1,\"regime\":\"NEW\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Annual income must not be negative")));

        mockMvc.perform(post("/api/tax/calculate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"annualIncome\":1000000,\"regime\":\"INVALID\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON or invalid request value."));

        mockMvc.perform(post("/api/tax/calculate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"annualIncome\":1000000,\"regime\":\"OLD\","
                                + "\"totalDeductions\":10000,\"section80C\":5000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Provide either total deductions or itemized deductions, not both."
                ));
    }

    private String registerAndLogin(String label) throws Exception {
        String email = label + "-" + UUID.randomUUID() + "@example.com";
        String password = "Business-" + UUID.randomUUID() + "8";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "fullName", "Core Business Test",
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(login.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
    }

    private long createCategory(
            String token,
            String name,
            String type,
            String color,
            String icon
    ) throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("name", name);
        request.put("type", type);
        if (color != null) {
            request.put("color", color);
        }
        if (icon != null) {
            request.put("icon", icon);
        }

        MvcResult result = mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void createTransaction(
            String token,
            long categoryId,
            String description,
            double amount,
            String type,
            LocalDate date
    ) throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "amount", amount,
                                "description", description,
                                "transactionDate", date.toString(),
                                "paymentMethod", "CARD",
                                "transactionType", type,
                                "categoryId", categoryId
                        ))))
                .andExpect(status().isCreated());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
