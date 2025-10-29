package com.clinicore.project.integration;

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.UserProfileRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for UserProfileController
 * Tests: Create user → Get user profile → Delete user
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private static Long testUserId;

    @Test
    @Order(1)
    @DisplayName("1. CREATE - Make new user in database")
    void createUser() {
        System.out.println("\n=== TEST 1: Creating User ===");

        UserProfile user = new UserProfile();
        user.setFirstName("Alice");
        user.setLastName("Smith");
        user.setGender("Female");
        user.setBirthday(LocalDate.of(1985, 3, 20));
        user.setContactNumber("555-9999");
        user.setUsername("alicesmith_test_" + System.currentTimeMillis());
        user.setPasswordHash("hashed_password");
        
        // Save and capture the generated ID
        UserProfile savedUser = userProfileRepository.save(user);
        testUserId = savedUser.getId();

        // Verify ID was generated
        assertNotNull(testUserId, "User ID should be generated");
        assertTrue(testUserId > 0, "User ID should be positive");

        System.out.println("✓ Created user with auto-generated ID: " + testUserId);
    }

    @Test
    @Order(2)
    @DisplayName("2. GET - Grab user information via endpoint")
    void getUserProfile() throws Exception {
        System.out.println("\n=== TEST 2: Getting User Profile ===");

        mockMvc.perform(get("/api/user/{id}/profile", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(testUserId))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Smith"))
                .andExpect(jsonPath("$.gender").value("Female"))
                .andExpect(jsonPath("$.birthday").value("1985-03-20"))
                .andExpect(jsonPath("$.contactNumber").value("555-9999"));

        System.out.println("✓ Retrieved user profile successfully with ID: " + testUserId);
    }

    @Test
    @Order(3)
    @DisplayName("3. DELETE - Delete user from database")
    void deleteUser() {
        System.out.println("\n=== TEST 3: Deleting User ===");

        userProfileRepository.deleteById(testUserId);
        
        // Verify deletion
        assertFalse(userProfileRepository.existsById(testUserId), "User should be deleted");

        System.out.println("✓ Deleted user with ID: " + testUserId);
    }
}