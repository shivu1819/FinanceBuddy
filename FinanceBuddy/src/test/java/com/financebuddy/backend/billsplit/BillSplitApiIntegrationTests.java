package com.financebuddy.backend.billsplit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financebuddy.backend.entity.User;
import com.financebuddy.backend.repository.UserRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BillSplitApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void authenticatedUserCanCreateViewListAndDeleteBillSplits() throws Exception {
        AuthenticatedUser owner = registerAndLogin("bill-owner");

        MvcResult createResult = mockMvc.perform(post("/api/bill-splits")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(equalSplitBody(
                                "Dinner",
                                "Dinner with friends",
                                LocalDate.now(),
                                "1200.00",
                                "Alice",
                                "Bob",
                                "Cara"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Dinner"))
                .andExpect(jsonPath("$.participants", hasSize(3)))
                .andExpect(jsonPath("$.participants[0].shareAmount").value(400))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long billSplitId = created.get("id").asLong();

        mockMvc.perform(get("/api/bill-splits")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(billSplitId))
                .andExpect(jsonPath("$[0].title").value("Dinner"));

        mockMvc.perform(get("/api/bill-splits/{id}", billSplitId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(billSplitId))
                .andExpect(jsonPath("$.participants", hasSize(3)))
                .andExpect(jsonPath("$.items", hasSize(0)));

        mockMvc.perform(delete("/api/bill-splits/{id}", billSplitId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Bill split deleted successfully."));

        mockMvc.perform(get("/api/bill-splits")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void billSplitOwnershipIsScopedToTheAuthenticatedUser() throws Exception {
        AuthenticatedUser owner = registerAndLogin("bill-owner-one");
        AuthenticatedUser otherUser = registerAndLogin("bill-owner-two");

        MvcResult createResult = mockMvc.perform(post("/api/bill-splits")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(equalSplitBody(
                                "Private dinner",
                                null,
                                LocalDate.now(),
                                "500.00",
                                "Alice",
                                "Bob"
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        long billSplitId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id")
                .asLong();

        mockMvc.perform(get("/api/bill-splits/{id}", billSplitId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherUser.token())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bill split not found."));

        mockMvc.perform(delete("/api/bill-splits/{id}", billSplitId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherUser.token())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bill split not found."));
    }

    @Test
    void customSplitRejectsItemsWithoutAssignments() throws Exception {
        AuthenticatedUser owner = registerAndLogin("bill-custom-validation");

        mockMvc.perform(post("/api/bill-splits")
                        .header(HttpHeaders.AUTHORIZATION, bearer(owner.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(customSplitBodyWithoutAssignments())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Every item must have at least one participant."));
    }

    private AuthenticatedUser registerAndLogin(String label) throws Exception {
        String email = label + "-" + UUID.randomUUID() + "@example.com";
        String password = "Bills-" + UUID.randomUUID() + "7";

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(Map.of(
                                "fullName", "Bill Split API Test",
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
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
        User user = userRepository.findByEmail(email).orElseThrow();
        return new AuthenticatedUser(token, user);
    }

    private Map<String, Object> equalSplitBody(
            String title,
            String description,
            LocalDate billDate,
            String subtotal,
            String... participants
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", title);
        if (description != null) {
            body.put("description", description);
        }
        body.put("billDate", billDate.toString());
        body.put("subtotal", subtotal);
        body.put("splitType", "EQUAL");
        body.put("participants", participantBody(participants));
        return body;
    }

    private Map<String, Object> customSplitBodyWithoutAssignments() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("title", "Custom split");
        body.put("billDate", LocalDate.now().toString());
        body.put("subtotal", "100.00");
        body.put("splitType", "CUSTOM");
        body.put("participants", participantBody("Alice", "Bob"));
        body.put("items", List.of(itemBody("Pizza", "1", "100.00")));
        return body;
    }

    private List<Map<String, Object>> participantBody(String... names) {
        return java.util.Arrays.stream(names)
                .map(name -> Map.<String, Object>of("participantName", name))
                .toList();
    }

    private Map<String, Object> itemBody(String itemName, String quantity, String unitPrice, String... participants) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("itemName", itemName);
        item.put("quantity", quantity);
        item.put("unitPrice", unitPrice);
        if (participants.length > 0) {
            item.put("participantNames", List.of(participants));
        }
        return item;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record AuthenticatedUser(String token, User user) {
    }
}
