package com.clinicore.project.integration;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.AccountCredentialRepository;
import com.clinicore.project.repository.InvitationRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Integration tests for AccountCredentialController
 * Tests login, invitation creation, and account registration flows
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
    private InvitationRepository invitationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Test User IDs
    private static final Long ADMIN_ID = 1L;
    private static String invitationToken;

    // ==================== LOGIN TESTS ====================

    @Test
    @Order(1)
    @DisplayName("TEST 1: Admin should login successfully")
    void testAdminLogin() throws Exception {
        System.out.println("\n=== TEST 1: Admin Login ===");

        // send login details to endpoint
        Map<String, Object> loginRequest = new LinkedHashMap<>();
        loginRequest.put("username", "knguyen");
        loginRequest.put("passwordHash", "hash_01");

        mockMvc.perform(post("/api/accountCredential/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("knguyen"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Resident should login successfully")
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
                .andExpect(jsonPath("$.role").value("RESIDENT"));

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Caregiver should login successfully")
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
                .andExpect(jsonPath("$.role").value("CAREGIVER"));

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Login with invalid credentials should fail")
    void testLoginInvalidCredentials() throws Exception {
        System.out.println("\n=== TEST 4: Login Invalid Credentials ===");

        Map<String, Object> loginRequest = new LinkedHashMap<>();
        loginRequest.put("username", "knguyen");
        loginRequest.put("passwordHash", "wrongpassword");

        mockMvc.perform(post("/api/accountCredential/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());

        System.out.println("PASSED TEST 4");
    }

    // ==================== INVITATION TESTS ====================

    @Test
    @Order(5)
    @DisplayName("TEST 5: Admin should create invitation for resident")
    void testAdminCreatesResidentInvitation() throws Exception {
        System.out.println("\n=== TEST 5: Admin Creates Resident Invitation ===");

        String uniqueEmail = "resident-" + System.currentTimeMillis() + "@example.com";

        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", ADMIN_ID);
        inviteRequest.put("email", uniqueEmail);
        inviteRequest.put("role", "RESIDENT");
    
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invitation created successfully"))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("RESIDENT"))
                .andExpect(jsonPath("$.token").exists())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    invitationToken = objectMapper.readTree(response).get("token").asText();
                });

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Admin should create invitation for caregiver")
    void testAdminCreatesCaregiverInvitation() throws Exception {
        System.out.println("\n=== TEST 6: Admin Creates Caregiver Invitation ===");

        String uniqueEmail = "caregiver-" + System.currentTimeMillis() + "@example.com";

        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", ADMIN_ID);
        inviteRequest.put("email", uniqueEmail);
        inviteRequest.put("role", "CAREGIVER");
    
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invitation created successfully"))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("CAREGIVER"))
                .andExpect(jsonPath("$.token").exists());

        System.out.println("PASSED TEST 6");
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: Admin should create invitation for another admin")
    void testAdminCreatesAdminInvitation() throws Exception {
        System.out.println("\n=== TEST 7: Admin Creates Admin Invitation ===");

        String uniqueEmail = "admin-" + System.currentTimeMillis() + "@example.com";

        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", ADMIN_ID);
        inviteRequest.put("email", uniqueEmail);
        inviteRequest.put("role", "ADMIN");
    
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Invitation created successfully"))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.token").exists());

        System.out.println("PASSED TEST 7");
    }

    @Test
    @Order(8)
    @DisplayName("TEST 8: Resident cannot create invitation")
    void testResidentCannotInvite() throws Exception {
        System.out.println("\n=== TEST 8: Resident Cannot Invite ===");

        String uniqueEmail = "test-" + System.currentTimeMillis() + "@example.com";

        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", 4L);
        inviteRequest.put("email", uniqueEmail);
        inviteRequest.put("role", "RESIDENT");
    
        // Resident ID is 4
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only admins can create invitations"));

        System.out.println("PASSED TEST 8");
    }

    @Test
    @Order(9)
    @DisplayName("TEST 9: Caregiver cannot create invitation")
    void testCaregiverCannotInvite() throws Exception {
        System.out.println("\n=== TEST 9: Caregiver Cannot Invite ===");

        String uniqueEmail = "test-" + System.currentTimeMillis() + "@example.com";

        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", 2L);
        inviteRequest.put("email", uniqueEmail);
        inviteRequest.put("role", "RESIDENT");
    
        // Caregiver ID is 2
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only admins can create invitations"));

        System.out.println("PASSED TEST 9");
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @Order(10)
    @DisplayName("TEST 10: Resident should register with valid invitation")
    void testResidentRegisterWithValidInvitation() throws Exception {
        System.out.println("\n=== TEST 10: Resident Register with Valid Invitation ===");

        // Step 1: Admin creates invitation for resident
        String residentEmail = "newresident-" + System.currentTimeMillis() + "@example.com";
        
        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", ADMIN_ID);
        inviteRequest.put("email", residentEmail);
        inviteRequest.put("role", "RESIDENT");
        
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    invitationToken = objectMapper.readTree(response).get("token").asText();
                });

        // Step 2: Resident accepts invitation and creates account
        Map<String, Object> registerRequest = new LinkedHashMap<>();
        registerRequest.put("token", invitationToken);
        registerRequest.put("firstName", "Alice");
        registerRequest.put("lastName", "Johnson");
        registerRequest.put("username", "alicejohnson-" + System.currentTimeMillis());
        registerRequest.put("password", "password123");
        registerRequest.put("gender", "Female");
        registerRequest.put("birthday", "1992-03-22");
        registerRequest.put("contactNumber", "555-5555");

        mockMvc.perform(post("/api/accountCredential/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account created successfully"))
                .andExpect(jsonPath("$.role").value("RESIDENT"))
                .andExpect(jsonPath("$.id").exists());

        System.out.println("PASSED TEST 10");
    }

    @Test
    @Order(11)
    @DisplayName("TEST 11: Caregiver should register with valid invitation")
    void testCaregiverRegisterWithValidInvitation() throws Exception {
        System.out.println("\n=== TEST 11: Caregiver Register with Valid Invitation ===");

        // Step 1: Admin creates invitation for caregiver
        String caregiverEmail = "newcaregiver-" + System.currentTimeMillis() + "@example.com";
        
        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", ADMIN_ID);
        inviteRequest.put("email", caregiverEmail);
        inviteRequest.put("role", "CAREGIVER");
        
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    invitationToken = objectMapper.readTree(response).get("token").asText();
                });

        // Step 2: Caregiver accepts invitation and creates account
        Map<String, Object> registerRequest = new LinkedHashMap<>();
        registerRequest.put("token", invitationToken);
        registerRequest.put("firstName", "Michael");
        registerRequest.put("lastName", "Brown");
        registerRequest.put("username", "michaelbrown-" + System.currentTimeMillis());
        registerRequest.put("password", "password123");
        registerRequest.put("gender", "Male");
        registerRequest.put("birthday", "1988-07-10");
        registerRequest.put("contactNumber", "555-6666");

        mockMvc.perform(post("/api/accountCredential/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account created successfully"))
                .andExpect(jsonPath("$.role").value("CAREGIVER"))
                .andExpect(jsonPath("$.id").exists());

        System.out.println("PASSED TEST 11");
    }

    @Test
    @Order(12)
    @DisplayName("TEST 12: New Admin should register with valid invitation")
    void testAdminRegisterWithValidInvitation() throws Exception {
        System.out.println("\n=== TEST 12: New Admin Register with Valid Invitation ===");

        // Step 1: Existing admin creates invitation for new admin
        String adminEmail = "newadmin-" + System.currentTimeMillis() + "@example.com";
        
        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", ADMIN_ID);
        inviteRequest.put("email", adminEmail);
        inviteRequest.put("role", "ADMIN");
        
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    invitationToken = objectMapper.readTree(response).get("token").asText();
                });

        // Step 2: New admin accepts invitation and creates account
        Map<String, Object> registerRequest = new LinkedHashMap<>();
        registerRequest.put("token", invitationToken);
        registerRequest.put("firstName", "Sarah");
        registerRequest.put("lastName", "Williams");
        registerRequest.put("username", "sarahwilliams-" + System.currentTimeMillis());
        registerRequest.put("password", "password123");
        registerRequest.put("gender", "Female");
        registerRequest.put("birthday", "1985-11-15");
        registerRequest.put("contactNumber", "555-7777");

        mockMvc.perform(post("/api/accountCredential/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account created successfully"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.id").exists());

        System.out.println("PASSED TEST 12");
    }

    @Test
    @Order(13)
    @DisplayName("TEST 13: Registration should fail if required fields are missing")
    void testRegisterWithMissingFields() throws Exception {
        System.out.println("\n=== TEST 15: Register with Missing Required Fields ===");

        // Create a valid invitation first
        String uniqueEmail = "missingfields-" + System.currentTimeMillis() + "@example.com";
        
        Map<String, Object> inviteRequest = new LinkedHashMap<>();
        inviteRequest.put("adminId", ADMIN_ID);
        inviteRequest.put("email", uniqueEmail);
        inviteRequest.put("role", "RESIDENT");
        
        mockMvc.perform(post("/api/accountCredential/invite")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inviteRequest)))
                .andExpect(status().isOk())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    invitationToken = objectMapper.readTree(response).get("token").asText();
                });

        // Try to register without token (should fail before even checking token)
        Map<String, Object> registerRequest = new LinkedHashMap<>();
        registerRequest.put("token", "");
        registerRequest.put("firstName", "John");
        registerRequest.put("lastName", "Doe");
        registerRequest.put("username", "johndoe-" + System.currentTimeMillis());
        registerRequest.put("password", "password123");
        registerRequest.put("gender", "Male");
        registerRequest.put("birthday", "1990-05-15");
        registerRequest.put("contactNumber", "555-1234");

        mockMvc.perform(post("/api/accountCredential/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("token")));

        System.out.println("PASSED TEST 13");
    }
}