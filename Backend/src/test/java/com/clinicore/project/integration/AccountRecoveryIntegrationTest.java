package com.clinicore.project.integration;

import com.clinicore.project.service.EmailService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountRecoveryIntegrationTest {

    @MockitoBean
    private EmailService emailService;

    @Autowired
    private com.clinicore.project.repository.AccountCreationRequestRepository accountCreationRequestRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String capturedResetToken;
    private static final String RESET_EMAIL = "knguyen@gmail.com";

    @Test
    @Order(1)
    @DisplayName("TEST 1: Should request password reset successfully")
    void testForgotPasswordRequest() throws Exception {
        System.out.println("\n=== TEST 1: Forgot Password Request ===");

        doAnswer(invocation -> {
            capturedResetToken = invocation.getArgument(1);
            return null;
        }).when(emailService).sendPasswordResetLink(anyString(), anyString());

        Map<String, String> request = new LinkedHashMap<>();
        request.put("email", RESET_EMAIL);

        mockMvc.perform(post("/api/accountCredential/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset email sent."));

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Forgot password with non-existent email")
    void testForgotPasswordNonExistentEmail() throws Exception {
        System.out.println("\n=== TEST 2: Forgot Password Non-Existent Email ===");

        Map<String, String> request = new LinkedHashMap<>();
        request.put("email", "doesnotexist@example.com");

        mockMvc.perform(post("/api/accountCredential/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Reset password and verify with new login")
    void testResetPasswordAndVerifyLogin() throws Exception {
        System.out.println("\n=== TEST 3: Reset Password & Verify Login ===");

        // Ensure we have the token from the previous 'Forgot Password' test
        Assumptions.assumeTrue(capturedResetToken != null, "Reset token was not captured in Test 1");

        String newPassword = "LGTM1234!";

        // Perform the Password Reset
        Map<String, String> resetRequest = new LinkedHashMap<>();
        resetRequest.put("token", capturedResetToken);
        resetRequest.put("newPassword", newPassword);

        mockMvc.perform(post("/api/accountCredential/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successfully."));

        // Log in with the new password
        Map<String, Object> loginRequest = new LinkedHashMap<>();
        loginRequest.put("username", "knguyen");
        loginRequest.put("passwordHash", newPassword);

        mockMvc.perform(post("/api/accountCredential/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("knguyen"))
                .andExpect(jsonPath("$.id").exists());

        System.out.println("PASSED TEST 3: Login verified with new password.");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Reset password with expired token should fail")
    void testResetPasswordExpiredToken() throws Exception {
        System.out.println("\n=== TEST 4: Reset Password Expired Token ===");

        String expiredToken = "expired-token-abc123";

        Map<String, String> resetRequest = new LinkedHashMap<>();
        resetRequest.put("token", expiredToken);
        resetRequest.put("newPassword", "LGTM1234!");

        mockMvc.perform(post("/api/accountCredential/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Forgot UserID with valid email")
    void testForgotUserIdValidEmail() throws Exception {
        System.out.println("\n=== TEST 5: Forgot UserID ===");

        Map<String, String> request = new LinkedHashMap<>();
        request.put("email", RESET_EMAIL);

        mockMvc.perform(post("/api/accountCredential/forgot-userid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Username sent to your email."));

        verify(emailService, times(1)).sendUsernameReminder(eq(RESET_EMAIL), anyString());

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Request creating a new account")
    void testRequestAccessCreatesPendingRequest() throws Exception {
        System.out.println("\n=== TEST 20: Request Access ===");

        String uniqueEmail = "user-" + System.currentTimeMillis() + "@example.com";

        Map<String, String> requestBody = new LinkedHashMap<>();
        requestBody.put("firstName", "John");
        requestBody.put("lastName", "Smith");
        requestBody.put("email", uniqueEmail);
        requestBody.put("role", "RESIDENT");

        mockMvc.perform(post("/api/accountCredential/request-access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NEW"));

        var savedRequest = accountCreationRequestRepository.findAll().stream()
                .filter(req -> req.getEmail().equals(uniqueEmail))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Account request was not saved to the database"));

        Assertions.assertEquals("John", savedRequest.getFirstName());
        Assertions.assertEquals("Smith", savedRequest.getLastName());
        Assertions.assertEquals("RESIDENT", savedRequest.getRole());

        System.out.println("PASSED TEST 6");
    }
}