package com.clinicore.project.integration;

import com.clinicore.project.entity.AccountCreationRequest;
import com.clinicore.project.entity.Invitation;
import com.clinicore.project.repository.AccountCreationRequestRepository;
import com.clinicore.project.repository.InvitationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountRequestsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountCreationRequestRepository accountCreationRequestRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    private static Long testRequestId;
    private static final Long ADMIN_ID = 1L;

    @Test
    @Order(1)
    @DisplayName("TEST 1: Admin lists pending requests")
    void testListPendingRequests() throws Exception {
        System.out.println("\n=== TEST 1: Admin lists pending requests ===");

        mockMvc.perform(get("/accountCredential/account-requests")
                        .param("adminId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(List.class)))
                .andExpect(jsonPath("$[*].status", hasItems("PENDING")));

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Admin approves a request → status becomes APPROVED, invitation created")
    void testApproveRequest() throws Exception {
        System.out.println("\n=== TEST 2: Admin approves a request ===");

        // First create a pending request
        AccountCreationRequest request = new AccountCreationRequest();
        request.setFirstName("Test");
        request.setLastName("User");
        request.setEmail("test.approve@example.com");
        request.setRole("RESIDENT");
        request.setStatus("PENDING");
        AccountCreationRequest saved = accountCreationRequestRepository.save(request);
        testRequestId = saved.getId();
        System.out.println("Created test request with ID: " + testRequestId);

        // Approve the request
        mockMvc.perform(post("/accountCredential/account-requests/{id}/approve", testRequestId)
                        .param("adminId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activationCode", notNullValue()));

        // Verify status is APPROVED in DB
        AccountCreationRequest updated = accountCreationRequestRepository.findById(testRequestId).orElse(null);
        assert updated != null;
        assert updated.getStatus().equals("APPROVED");
        System.out.println("Verified status is APPROVED in database");

        // Verify invitation was created
        Optional<Invitation> invitation = invitationRepository.findByEmail("test.approve@example.com");
        assert invitation.isPresent();
        System.out.println("Verified invitation was created in database");

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Admin rejects a request → status becomes DENIED")
    void testRejectRequest() throws Exception {
        System.out.println("\n=== TEST 3: Admin rejects a request ===");

        // Create a pending request
        AccountCreationRequest request = new AccountCreationRequest();
        request.setFirstName("Reject");
        request.setLastName("User");
        request.setEmail("test.reject@example.com");
        request.setRole("CAREGIVER");
        request.setStatus("PENDING");
        AccountCreationRequest saved = accountCreationRequestRepository.save(request);
        Long rejectId = saved.getId();
        System.out.println("Created test request with ID: " + rejectId);

        // Reject the request
        mockMvc.perform(post("/accountCredential/account-requests/{id}/deny", rejectId)
                        .param("adminId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        // Verify status is DENIED in DB
        AccountCreationRequest updated = accountCreationRequestRepository.findById(rejectId).orElse(null);
        assert updated != null;
        assert updated.getStatus().equals("DENIED");
        System.out.println("Verified status is DENIED in database");

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Approving an already-approved request returns 400/409")
    void testApproveAlreadyApprovedRequest() throws Exception {
        System.out.println("\n=== TEST 4: Approving already-approved request ===");

        mockMvc.perform(post("/accountCredential/account-requests/{id}/approve", testRequestId)
                        .param("adminId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Rejecting non-existent id returns 404")
    void testRejectNonExistentRequest() throws Exception {
        System.out.println("\n=== TEST 5: Rejecting non-existent request ===");

        mockMvc.perform(post("/accountCredential/account-requests/{id}/deny", 99999L)
                        .param("adminId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());

        System.out.println("PASSED TEST 5");
    }
}