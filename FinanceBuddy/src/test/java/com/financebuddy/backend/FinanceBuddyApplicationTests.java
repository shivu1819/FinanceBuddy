package com.financebuddy.backend;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class FinanceBuddyApplicationTests {

    @Autowired
    private Flyway flyway;

    @Test
    void contextLoads() {
    }

    @Test
    void databaseMigrationsAreAtVersionSevenWithNothingPending() {
        assertEquals("7", flyway.info().current().getVersion().getVersion());
        assertEquals(0, flyway.info().pending().length);
    }

}
