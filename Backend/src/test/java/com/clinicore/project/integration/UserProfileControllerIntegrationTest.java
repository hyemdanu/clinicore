package com.clinicore.project.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for UserProfileController
 * Tests: Read user profiles from pre-populated database
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final Long ADMIN_ID = 1L;
    private static final Long RESIDENT_ID = 4L;

    @Test
    @Order(1)
    @DisplayName("1. READ - Get admin user profile")
    void getAdminProfile() throws Exception {
        System.out.println("\n=== TEST 1: Reading Admin Profile ===");

        mockMvc.perform(get("/api/user/{userProfileId}/profile", ADMIN_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(ADMIN_ID.intValue()))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Administrator"))
                .andExpect(jsonPath("$.username").value("admin_user"));

        System.out.println("✓ Successfully retrieved admin profile");
    }

    @Test
    @Order(2)
    @DisplayName("2. READ - Get resident user profile")
    void getResidentProfile() throws Exception {
        System.out.println("\n=== TEST 2: Reading Resident Profile ===");

        mockMvc.perform(get("/api/user/{userProfileId}/profile", RESIDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(RESIDENT_ID.intValue()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.gender").value("Male"));

        System.out.println("✓ Successfully retrieved resident profile");
    }

    @Test
    @Order(3)
    @DisplayName("3. READ - Get caregiver profile")
    void getCaregiverProfile() throws Exception {
        System.out.println("\n=== TEST 3: Reading Caregiver Profile ===");

        mockMvc.perform(get("/api/user/{userProfileId}/profile", 2L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Bob"))
                .andExpect(jsonPath("$.lastName").value("Caregiver"))
                .andExpect(jsonPath("$.username").value("caregiver_bob"));

        System.out.println("✓ Successfully retrieved caregiver profile");
    }

    @Test
    @Order(4)
    @DisplayName("4. READ - Get non-existent user (error handling)")
    void getUserNotFound() throws Exception {
        System.out.println("\n=== TEST 4: Reading Non-Existent User ===");

        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/user/{userProfileId}/profile", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Userprofile not found"));

        System.out.println("✓ Correctly returned 404 for non-existent user");
    }
}