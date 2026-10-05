package com.financebuddy.backend.billsplit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BillSplitSecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void billSplitApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/bill-splits"))
                .andExpect(status().isUnauthorized());
    }
}
