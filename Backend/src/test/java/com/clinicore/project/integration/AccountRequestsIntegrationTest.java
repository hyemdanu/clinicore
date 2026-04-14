package com.clinicore.project.integration;

import com.clinicore.project.entity.AccountCreationRequest;
import com.clinicore.project.repository.AccountCreationRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountRequestsIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AccountCreationRequestRepository requestRepo;
    @Autowired private ObjectMapper objectMapper;

    private static final Long ADMIN_ID = 1L;

    // Shared across ordered tests 1 → 3 → 4 (same request lifecycle)
    private Long testRequestId;

    private static final List<String> TEST_EMAILS = List.of(
            "test.patient@example.com",
            "deny.me@example.com",
            "double.approve@example.com"
    );

    /** Runs once before any test — ensures a clean slate. */
    @BeforeAll
    void cleanUpBeforeAll() {
        TEST_EMAILS.forEach(email ->
                requestRepo.findByEmail(email).ifPresent(r -> {
                    requestRepo.delete(r);
                    requestRepo.flush();
                }));
    }

    /** Runs once after all tests — leaves the DB clean. */
    @AfterAll
    void cleanUpAfterAll() {
        TEST_EMAILS.forEach(email ->
                requestRepo.findByEmail(email).ifPresent(r -> {
                    requestRepo.delete(r);
                    requestRepo.flush();
                }));
    }

    // -------------------------------------------------------------------------
    // TEST 1 — Create pending request
    // -------------------------------------------------------------------------

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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"));

        testRequestId = requestRepo.findByEmail("test.patient@example.com")
                .orElseThrow()
                .getId();

        AccountCreationRequest saved = requestRepo.findById(testRequestId).orElseThrow();
        Assertions.assertEquals("PENDING", saved.getStatus());

        System.out.println("PASSED TEST 1 — requestId=" + testRequestId);
    }

    // -------------------------------------------------------------------------
    // TEST 2 — Admin lists pending requests (200 + array)
    // -------------------------------------------------------------------------

    @Test
    @Order(2)
    @DisplayName("TEST 2: Admin lists account requests — 200 + array")
    void testListRequests() throws Exception {
        System.out.println("\n=== TEST 2: List Requests ===");

        mockMvc.perform(get("/api/accountCredential/account-requests")
                        .param("adminId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 2");
    }

    // -------------------------------------------------------------------------
    // TEST 3 — Approve request → APPROVED in DB, activation code returned
    // -------------------------------------------------------------------------

    @Test
    @Order(3)
    @DisplayName("TEST 3: Admin approves request — status APPROVED in DB, activation code returned")
    void testApproveRequest() throws Exception {
        System.out.println("\n=== TEST 3: Approve Request ===");

        mockMvc.perform(post("/api/accountCredential/account-requests/" + testRequestId + "/approve")
                        .param("adminId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activationCode").exists());

        // DB verification
        AccountCreationRequest saved = requestRepo.findById(testRequestId).orElseThrow();
        Assertions.assertEquals("APPROVED", saved.getStatus());
        Assertions.assertNotNull(saved.getActivationCodeHash(), "activation code hash must be stored");
        Assertions.assertEquals(ADMIN_ID, saved.getApprovedByAdminId());

        System.out.println("PASSED TEST 3");
    }

    // -------------------------------------------------------------------------
    // TEST 4 — Resend activation code on already-approved request
    // -------------------------------------------------------------------------

    @Test
    @Order(4)
    @DisplayName("TEST 4: Resend activation code on approved request")
    void testResendActivationCode() throws Exception {
        System.out.println("\n=== TEST 4: Resend Activation Code ===");

        mockMvc.perform(post("/api/accountCredential/account-requests/" + testRequestId + "/resend-activation-code")
                        .param("adminId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activationCode").exists());

        System.out.println("PASSED TEST 4");
    }

    // -------------------------------------------------------------------------
    // TEST 5 — Non-admin call is rejected with 403
    // -------------------------------------------------------------------------

    @Test
    @Order(5)
    @DisplayName("TEST 5: Non-admin call to list requests is rejected with 403")
    void testNonAdminRejected() throws Exception {
        System.out.println("\n=== TEST 5: Non-Admin Rejected ===");

        // ID 2 is not an admin — validateAdmin throws IllegalArgumentException
        // → controller returns 403 FORBIDDEN (see getAllAccountRequests catch block)
        mockMvc.perform(get("/api/accountCredential/account-requests")
                        .param("adminId", "2"))
                .andExpect(status().isForbidden());

        System.out.println("PASSED TEST 5");
    }

    // -------------------------------------------------------------------------
    // TEST 6 — Admin denies a request → status DENIED in DB
    // -------------------------------------------------------------------------

    @Test
    @Order(6)
    @DisplayName("TEST 6: Admin denies a request — status DENIED in DB")
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

        Long denyId = requestRepo.findByEmail("deny.me@example.com").orElseThrow().getId();

        mockMvc.perform(post("/api/accountCredential/account-requests/" + denyId + "/deny")
                        .param("adminId", ADMIN_ID.toString())
                        .param("reason", "test reason"))
                .andExpect(status().isOk());

        AccountCreationRequest saved = requestRepo.findById(denyId).orElseThrow();
        Assertions.assertEquals("DENIED", saved.getStatus());

        System.out.println("PASSED TEST 6");
    }

    // -------------------------------------------------------------------------
    // TEST 7 — Approving an already-approved request returns 400
    // -------------------------------------------------------------------------

    @Test
    @Order(7)
    @DisplayName("TEST 7: Approving an already-approved request returns 400")
    void testApproveAlreadyApproved() throws Exception {
        System.out.println("\n=== TEST 7: Double Approve Returns 400 ===");

        // Create a fresh PENDING request
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", "Double");
        body.put("lastName", "Approve");
        body.put("email", "double.approve@example.com");
        body.put("role", "CAREGIVER");

        mockMvc.perform(post("/api/accountCredential/request-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        Long doubleId = requestRepo.findByEmail("double.approve@example.com").orElseThrow().getId();

        // First approval — must succeed
        mockMvc.perform(post("/api/accountCredential/account-requests/" + doubleId + "/approve")
                        .param("adminId", ADMIN_ID.toString()))
                .andExpect(status().isOk());

        Assertions.assertEquals("APPROVED", requestRepo.findById(doubleId).orElseThrow().getStatus());

        // Second approval on the same (now APPROVED) request — must return 400
        mockMvc.perform(post("/api/accountCredential/account-requests/" + doubleId + "/approve")
                        .param("adminId", ADMIN_ID.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Status must still be APPROVED (not corrupted)
        Assertions.assertEquals("APPROVED", requestRepo.findById(doubleId).orElseThrow().getStatus());

        System.out.println("PASSED TEST 7");
    }

    // -------------------------------------------------------------------------
    // TEST 8 — Denying a non-existent request returns 404
    // -------------------------------------------------------------------------

    @Test
    @Order(8)
    @DisplayName("TEST 8: Denying a non-existent request returns error")
    void testDenyNonExistentRequest() throws Exception {
        System.out.println("\n=== TEST 8: Deny Non-Existent Request ===");

        long nonExistentId = Long.MAX_VALUE;

        mockMvc.perform(post("/api/accountCredential/account-requests/" + nonExistentId + "/deny")
                        .param("adminId", ADMIN_ID.toString())
                        .param("reason", "does not matter"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").exists());

        System.out.println("PASSED TEST 8");
    }
}
