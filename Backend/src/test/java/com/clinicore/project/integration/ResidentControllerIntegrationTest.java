package com.clinicore.project.integration;

import com.clinicore.project.entity.Resident;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.ResidentGeneralRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for ResidentGeneralController
 * Tests the actual REST API endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)  // Disable security for tests
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;  // Simulates HTTP requests

    @Autowired
    private ResidentGeneralRepository residentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private static Long testUserProfileId = 888888L;
    private static Long testResidentId = 888888L;

    @Test
    @Order(1)
    @DisplayName("Setup - Create test data")
    void setupTestData() {
        // Create UserProfile
        UserProfile userProfile = new UserProfile();
        userProfile.setId(testUserProfileId);
        userProfile.setFirstName("Jane");
        userProfile.setLastName("Smith");
        userProfile.setGender("Female");
        userProfile.setBirthday(LocalDate.of(1960, 3, 20));
        userProfile.setContactNumber("555-1234");
        userProfile.setUsername("janesmith_test_" + System.currentTimeMillis());
        userProfile.setPasswordHash("hashed_password_456");
        userProfileRepository.save(userProfile);

        // Create Resident
        Resident resident = new Resident();
        resident.setId(testResidentId);
        resident.setEmergencyContactName("John Smith");
        resident.setEmergencyContactNumber("555-5678");
        resident.setMedicalProfileId(null);
        resident.setNotes("Controller test resident");
        residentRepository.save(resident);

        System.out.println("✓ Test data created");
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/resident/{id}/general - Success")
    void testGetResidentGeneral_Success() throws Exception {
        mockMvc.perform(get("/api/resident/{userProfileId}/general", testUserProfileId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())  // Expect 200 OK
                .andExpect(jsonPath("$.userProfileId").value(testUserProfileId))
                .andExpect(jsonPath("$.username").value(containsString("janesmith_test")))
                .andExpect(jsonPath("$.gender").value("Female"))
                .andExpect(jsonPath("$.emergencyContactName").value("John Smith"))
                .andExpect(jsonPath("$.emergencyContactNumber").value("555-5678"))
                .andExpect(jsonPath("$.notes").value("Controller test resident"));

        System.out.println("✓ GET request successful - returned correct resident data");
    }

    @Test
    @Order(3)
    @DisplayName("GET /api/resident/{id}/general - User Not Found")
    void testGetResidentGeneral_UserNotFound() throws Exception {
        Long nonExistentId = 999999999L;

        mockMvc.perform(get("/api/resident/{userProfileId}/general", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())  // Expect 404 Not Found
                .andExpect(jsonPath("$.message").value("UserProfile not found"))
                .andExpect(jsonPath("$.userProfileId").value(nonExistentId));

        System.out.println("✓ Correctly returns 404 for non-existent user");
    }

    @Test
    @Order(4)
    @DisplayName("Cleanup - Remove test data")
    void cleanupTestData() {
        residentRepository.deleteById(testResidentId);
        userProfileRepository.deleteById(testUserProfileId);
        System.out.println("✓ Test data cleaned up");
    }
}
