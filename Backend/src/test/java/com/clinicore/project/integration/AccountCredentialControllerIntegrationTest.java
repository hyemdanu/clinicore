package com.clinicore.project.integration;

import com.clinicore.project.repository.AccountCreationRequestRepository;
import com.clinicore.project.repository.AccountCredentialRepository;
import com.clinicore.project.repository.UserProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AccountCredentialController
 * Covers login (with JWT) and the public /request-access endpoint.
 *
 * The full approve/verify/create-account flow is covered by AccountRequestsIntegrationTest.
 * Password reset is covered by AccountRecoveryIntegrationTest.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountCredentialControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountCredentialRepository accountCredentialRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AccountCreationRequestRepository accountCreationRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================== LOGIN TESTS FIX ====================

    @Test
    @Order(1)
    @DisplayName("TEST 1: Admin should login successfully and receive JWT")
    void testAdminLogin() throws Exception {
        System.out.println("\n=== TEST 1: Admin Login ===");

        Map<String, Object> loginRequest = new LinkedHashMap<>();
        loginRequest.put("username", "knguyen");
        loginRequest.put("passwordHash", "hash_01");

        mockMvc.perform(post("/api/accountCredential/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("knguyen"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token", not(emptyString())));

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Resident should login successfully and receive JWT")
    void testResidentLogin() throws Exception {
        System.out.println("\n=== TEST 2: Resident Login ===");

        Map<String, Object> loginRequest = new LinkedHashMap<>();
        loginRequest.put("username", "schoi");
        loginRequest.put("passwordHash", "hash_04");

        mockMvc.perform(post("/api/accountCredential/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.username").value("schoi"))
                .andExpect(jsonPath("$.role").value("RESIDENT"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token", not(emptyString())));

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Caregiver should login successfully and receive JWT")
    void testCaregiverLogin() throws Exception {
        System.out.println("\n=== TEST 3: Caregiver Login ===");

        Map<String, Object> loginRequest = new LinkedHashMap<>();
        loginRequest.put("username", "etran");
        loginRequest.put("passwordHash", "hash_02");

        mockMvc.perform(post("/api/accountCredential/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("etran"))
                .andExpect(jsonPath("$.role").value("CAREGIVER"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token", not(emptyString())));

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Login with wrong password should return 401")
    void testLoginInvalidCredentials() throws Exception {
        System.out.println("\n=== TEST 4: Login Invalid Credentials ===");

        Map<String, Object> loginRequest = new LinkedHashMap<>();
        loginRequest.put("username", "knguyen");
        loginRequest.put("passwordHash", "wrongpassword");

        mockMvc.perform(post("/api/accountCredential/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Login with unknown username should return 401")
    void testLoginUnknownUser() throws Exception {
        System.out.println("\n=== TEST 5: Login Unknown User ===");

        Map<String, Object> loginRequest = new LinkedHashMap<>();
        loginRequest.put("username", "no-such-user-" + System.currentTimeMillis());
        loginRequest.put("passwordHash", "anything");

        mockMvc.perform(post("/api/accountCredential/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        System.out.println("PASSED TEST 5");
    }

    // ==================== REQUEST-ACCESS TESTS ====================

    @Test
    @Order(6)
    @DisplayName("TEST 6: New email should create account request successfully")
    void testRequestAccessNewEmail() throws Exception {
        System.out.println("\n=== TEST 6: Request Access (New Email) ===");

        String uniqueEmail = "request-" + System.currentTimeMillis() + "@example.com";

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("firstName", "Alice");
        req.put("lastName", "Johnson");
        req.put("email", uniqueEmail);
        req.put("role", "RESIDENT");

        mockMvc.perform(post("/api/accountCredential/request-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.message").exists());

        // cleanup
        accountCreationRequestRepository.findByEmail(uniqueEmail)
                .ifPresent(accountCreationRequestRepository::delete);

        System.out.println("PASSED TEST 6");
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: Request access with missing fields should return 400")
    void testRequestAccessMissingFields() throws Exception {
        System.out.println("\n=== TEST 7: Request Access (Missing Fields) ===");

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("firstName", "");
        req.put("lastName", "Johnson");
        req.put("email", "x@y.com");
        req.put("role", "RESIDENT");

        mockMvc.perform(post("/api/accountCredential/request-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Missing")));

        System.out.println("PASSED TEST 7");
    }

    @Test
    @Order(8)
    @DisplayName("TEST 8: Request access with invalid email should return 400")
    void testRequestAccessInvalidEmail() throws Exception {
        System.out.println("\n=== TEST 8: Request Access (Invalid Email) ===");

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("firstName", "Alice");
        req.put("lastName", "Johnson");
        req.put("email", "not-an-email");
        req.put("role", "RESIDENT");

        mockMvc.perform(post("/api/accountCredential/request-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("email")));

        System.out.println("PASSED TEST 8");
    }

    @Test
    @Order(9)
    @DisplayName("TEST 9: Request access for existing user email returns USER_ALREADY_EXISTS")
    void testRequestAccessExistingUser() throws Exception {
        System.out.println("\n=== TEST 9: Request Access (Existing User) ===");

        // knguyen is a seeded admin — pull their real email from the DB
        String existingEmail = userProfileRepository.findById(1L)
                .map(u -> u.getEmail())
                .orElseThrow();

        Map<String, Object> req = new LinkedHashMap<>();
        req.put("firstName", "Anyone");
        req.put("lastName", "Else");
        req.put("email", existingEmail);
        req.put("role", "RESIDENT");

        mockMvc.perform(post("/api/accountCredential/request-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USER_ALREADY_EXISTS"));

        System.out.println("PASSED TEST 9");
    }
}
