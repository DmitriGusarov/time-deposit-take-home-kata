package org.ikigaidigital;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test through both REST endpoints against a real PostgreSQL started by Testcontainers.
 * Flyway runs V1 (schema) and V2 (seed data) on the fresh container, so the expected numbers
 * below follow directly from the seed rows in V2__seed_time_deposits.sql.
 * <p>
 * Requires Docker. Runs in the {@code verify} phase (failsafe), not in {@code test}.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TimeDepositApiIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Test
    @Order(1)
    void getReturnsAllSeededDepositsWithWithdrawals() throws Exception {
        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].planType").value("basic"))
                .andExpect(jsonPath("$[0].balance").value(1000.00))
                .andExpect(jsonPath("$[0].days").value(45))
                .andExpect(jsonPath("$[0].withdrawals", hasSize(2)))
                .andExpect(jsonPath("$[0].withdrawals[0].id").value(1))
                .andExpect(jsonPath("$[0].withdrawals[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].withdrawals[0].date").value("2026-09-15"))
                .andExpect(jsonPath("$[1].withdrawals", hasSize(0)));
    }

    @Test
    @Order(2)
    void postUpdatesEveryBalanceAccordingToItsPlan() throws Exception {
        mockMvc.perform(post("/time-deposits/balances"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/time-deposits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].balance").value(1000.83))   // basic, 45d
                .andExpect(jsonPath("$[1].balance").value(2500.00))   // basic, 30d: no interest yet
                .andExpect(jsonPath("$[2].balance").value(1203.00))   // student, 31d
                .andExpect(jsonPath("$[3].balance").value(3000.00))   // student, 366d: stopped
                .andExpect(jsonPath("$[4].balance").value(10000.00))  // premium, 45d: not started
                .andExpect(jsonPath("$[5].balance").value(1205.00))   // premium, 46d
                .andExpect(jsonPath("$[6].balance").value(7531.75));  // premium, 400d
    }
}
