package com.clinicore.project.integration;

import com.clinicore.project.repository.AccountCreationRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountRequestsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountCreationRequestRepository requestRepo;
    @Autowired private ObjectMapper objectMapper;

    private static final Long ADMIN_ID = 1L;
    private static Long testRequestId;

    @Test
    @Order(1)
    @DisplayName("TEST 1: Create a pending request via request-access")
    void testCreatePendingRequest() throws Exception {
        System.out.println("\n=== TEST 1: Create Pending Request ===");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", "Test");
        body.put("lastName", "Patient");
        body.put("email", "test.patient@example.com");
        body.put("role", "RESIDENT");

        mockMvc.perform(post("/api/accountCredential/request-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        testRequestId = requestRepo.findAll().stream()
                .filter(r -> "test.patient@example.com".equals(r.getEmail()))
                .findFirst().orElseThrow().getId();

        System.out.println("PASSED TEST 1 — requestId=" + testRequestId);
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Admin lists account requests")
    void testListRequests() throws Exception {
        System.out.println("\n=== TEST 2: List Requests ===");

        mockMvc.perform(get("/api/accountCredential/account-requests")
                        .param("adminId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Admin approves request -> activation code returned")
    void testApproveRequest() throws Exception {
        System.out.println("\n=== TEST 3: Approve Request ===");

        mockMvc.perform(post("/api/accountCredential/account-requests/" + testRequestId + "/approve")
                        .param("adminId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activationCode").exists());

        String status = requestRepo.findById(testRequestId).orElseThrow().getStatus().toString();
        Assertions.assertEquals("APPROVED", status);

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Resend activation code on approved request")
    void testResendActivationCode() throws Exception {
        System.out.println("\n=== TEST 4: Resend Activation Code ===");

        mockMvc.perform(post("/api/accountCredential/account-requests/" + testRequestId + "/resend-activation-code")
                        .param("adminId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activationCode").exists());

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Non-admin call to list requests is rejected")
    void testNonAdminRejected() throws Exception {
        System.out.println("\n=== TEST 5: Non-Admin Rejected ===");

        mockMvc.perform(get("/api/accountCredential/account-requests")
                        .param("adminId", "2"))
                .andExpect(status().isBadRequest());

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Deny a new request")
    void testDenyRequest() throws Exception {
        System.out.println("\n=== TEST 6: Deny Request ===");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", "Deny");
        body.put("lastName", "Me");
        body.put("email", "deny.me@example.com");
        body.put("role", "RESIDENT");

        mockMvc.perform(post("/api/accountCredential/request-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        Long denyId = requestRepo.findAll().stream()
                .filter(r -> "deny.me@example.com".equals(r.getEmail()))
                .findFirst().orElseThrow().getId();

        mockMvc.perform(post("/api/accountCredential/account-requests/" + denyId + "/deny")
                        .param("adminId", ADMIN_ID.toString())
                        .param("reason", "test reason")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        String status = requestRepo.findById(denyId).orElseThrow().getStatus().toString();
        Assertions.assertEquals("DENIED", status);

        System.out.println("PASSED TEST 6");
    }
}