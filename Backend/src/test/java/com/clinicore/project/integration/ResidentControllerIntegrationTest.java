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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for UserProfileController
 * Tests role-based access control for Resident, Caregiver, and Admin
 * Covers: READ, UPDATE, and RESET operations
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentControllerIntegrationTest {

    // this is to simulate the HTTP request/response cycle
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResidentGeneralRepository residentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Test User IDs
    private static final Long RESIDENT_ID = 4L;      // Sophia Choi
    private static final Long CAREGIVER_ID = 2L;     // Emily Tran
    private static final Long ADMIN_ID = 1L;         // Kevin Nguyen
    
    // Original values for reset
    private static final String ORIGINAL_RESIDENT_CONTACT = "Jane Doe";
    private static final String ORIGINAL_RESIDENT_PHONE = "555-9999";

    // ==================== RESIDENT TESTS ====================

    @Test
    @Order(1)
    @DisplayName("TEST 1: Resident should read own information")
    void testResidentReadOwnInfo() throws Exception {
        System.out.println("\n=== TEST 1: Resident Reading Own Information ===");

        mockMvc.perform(get("/api/user/{userProfileId}/profile", RESIDENT_ID)
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(RESIDENT_ID.intValue()))
                .andExpect(jsonPath("$.firstName").value("Sophia"))
                .andExpect(jsonPath("$.lastName").value("Choi"))
                .andExpect(jsonPath("$.role").value("RESIDENT"))
                .andExpect(jsonPath("$.emergencyContactName").exists())
                .andExpect(jsonPath("$.emergencyContactNumber").exists());

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Resident should update own information")
    void testResidentUpdateOwnInfo() throws Exception {
        System.out.println("\n=== TEST 2: Resident Updating Own Information ===");

        // Get resident from database
        Resident resident = residentRepository.findById(RESIDENT_ID).orElse(null);
        assertNotNull(resident, "Resident should exist");

        // Update using setter functions
        resident.setEmergencyContactName("Updated Contact Name");
        resident.setEmergencyContactNumber("555-0000");
        residentRepository.save(resident);

        // Verify update via API endpoint
        mockMvc.perform(get("/api/user/{userProfileId}/profile", RESIDENT_ID)
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emergencyContactName").value("Updated Contact Name"))
                .andExpect(jsonPath("$.emergencyContactNumber").value("555-0000"));

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Reset resident update")
    void testResetResidentUpdate() throws Exception {
        System.out.println("\n=== TEST 3: Reset Resident Update ===");

        // Get resident from database
        Resident resident = residentRepository.findById(RESIDENT_ID).orElse(null);
        assertNotNull(resident, "Resident should exist");

        // Reset using setter functions
        resident.setEmergencyContactName(ORIGINAL_RESIDENT_CONTACT);
        resident.setEmergencyContactNumber(ORIGINAL_RESIDENT_PHONE);
        residentRepository.save(resident);

        // Verify reset via API endpoint
        mockMvc.perform(get("/api/user/{userProfileId}/profile", RESIDENT_ID)
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emergencyContactName").value(ORIGINAL_RESIDENT_CONTACT))
                .andExpect(jsonPath("$.emergencyContactNumber").value(ORIGINAL_RESIDENT_PHONE));

        System.out.println("PASSED TEST 3");
    }

    // ==================== CAREGIVER TESTS ====================

    @Test
    @Order(4)
    @DisplayName("TEST 4: Caregiver should read own information")
    void testCaregiverReadOwnInfo() throws Exception {
        System.out.println("\n=== TEST 4: Caregiver Reading Own Information ===");

        mockMvc.perform(get("/api/user/{userProfileId}/profile", CAREGIVER_ID)
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(CAREGIVER_ID.intValue()))
                .andExpect(jsonPath("$.role").value("CAREGIVER"))
                .andExpect(jsonPath("$.caregiverNotes").exists());

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Caregiver should update own information")
    void testCaregiverUpdateOwnInfo() throws Exception {
        System.out.println("\n=== TEST 5: Caregiver Updating Own Information ===");

        // Get caregiver user profile from database
        UserProfile caregiver = userProfileRepository.findById(CAREGIVER_ID).orElse(null);
        assertNotNull(caregiver, "Caregiver should exist");

        // Store original contact number for reset
        String originalContactNumber = caregiver.getContactNumber();

        // Update using setter functions
        caregiver.setContactNumber("555-8888");
        userProfileRepository.save(caregiver);

        // Verify update via API endpoint
        mockMvc.perform(get("/api/user/{userProfileId}/profile", CAREGIVER_ID)
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactNumber").value("555-8888"));

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Reset caregiver update")
    void testResetCaregiverUpdate() throws Exception {
        System.out.println("\n=== TEST 6: Reset Caregiver Update ===");

        // Get caregiver user profile from database
        UserProfile caregiver = userProfileRepository.findById(CAREGIVER_ID).orElse(null);
        assertNotNull(caregiver, "Caregiver should exist");

        // Store original value (before test 5 ran)
        String originalContactNumber = "916-555-0144";

        // Reset using setter functions
        caregiver.setContactNumber(originalContactNumber);
        userProfileRepository.save(caregiver);

        // Verify reset via API endpoint
        mockMvc.perform(get("/api/user/{userProfileId}/profile", CAREGIVER_ID)
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactNumber").value(originalContactNumber));

        System.out.println("PASSED TEST 6");
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: Caregiver should read all residents")
    void testCaregiverReadAllResidents() throws Exception {
        System.out.println("\n=== TEST 7: Caregiver Reading All Residents ===");

        mockMvc.perform(get("/api/user/residents/list")
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[*].id").exists())
                .andExpect(jsonPath("$[*].firstName").exists())
                .andExpect(jsonPath("$[*].lastName").exists());

        System.out.println("PASSED TEST 7");
    }

    @Test
    @Order(8)
    @DisplayName("TEST 8: Caregiver should read a specific resident")
    void testCaregiverReadSpecificResident() throws Exception {
        System.out.println("\n=== TEST 8: Caregiver Reading Specific Resident ===");

        mockMvc.perform(get("/api/user/{userProfileId}/profile", RESIDENT_ID)
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(RESIDENT_ID.intValue()))
                .andExpect(jsonPath("$.role").value("RESIDENT"))
                .andExpect(jsonPath("$.emergencyContactName").exists());

        System.out.println("PASSED TEST 8");
    }

    // ==================== ADMIN TESTS ====================

    @Test
    @Order(9)
    @DisplayName("TEST 9: Admin should read own information")
    void testAdminReadOwnInfo() throws Exception {
        System.out.println("\n=== TEST 9: Admin Reading Own Information ===");

        mockMvc.perform(get("/api/user/{userProfileId}/profile", ADMIN_ID)
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(ADMIN_ID.intValue()))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        System.out.println("PASSED TEST 9");
    }

    @Test
    @Order(10)
    @DisplayName("TEST 10: Admin should update own information")
    void testAdminUpdateOwnInfo() throws Exception {
        System.out.println("\n=== TEST 10: Admin Updating Own Information ===");

        // Get admin user profile from database
        UserProfile admin = userProfileRepository.findById(ADMIN_ID).orElse(null);
        assertNotNull(admin, "Admin should exist");

        // Update using setter functions
        admin.setContactNumber("555-7777");
        userProfileRepository.save(admin);

        // Verify update via API endpoint
        mockMvc.perform(get("/api/user/{userProfileId}/profile", ADMIN_ID)
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactNumber").value("555-7777"));

        System.out.println("PASSED TEST 10");
    }

    @Test
    @Order(11)
    @DisplayName("TEST 11: Reset admin update")
    void testResetAdminUpdate() throws Exception {
        System.out.println("\n=== TEST 11: Reset Admin Update ===");

        // Get admin user profile from database
        UserProfile admin = userProfileRepository.findById(ADMIN_ID).orElse(null);
        assertNotNull(admin, "Admin should exist");

        // Store original value (before test 10 ran)
        String originalContactNumber = "916-555-0123";

        // Reset using setter functions
        admin.setContactNumber(originalContactNumber);
        userProfileRepository.save(admin);

        // Verify reset via API endpoint
        mockMvc.perform(get("/api/user/{userProfileId}/profile", ADMIN_ID)
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactNumber").value(originalContactNumber));

        System.out.println("PASSED TEST 11");
    }

    @Test
    @Order(12)
    @DisplayName("TEST 12: Admin should read all residents")
    void testAdminReadAllResidents() throws Exception {
        System.out.println("\n=== TEST 12: Admin Reading All Residents ===");

        mockMvc.perform(get("/api/user/residents/list")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[*].id").exists());

        System.out.println("PASSED TEST 12");
    }

    @Test
    @Order(13)
    @DisplayName("TEST 13: Admin should read a specific resident")
    void testAdminReadSpecificResident() throws Exception {
        System.out.println("\n=== TEST 13: Admin Reading Specific Resident ===");

        mockMvc.perform(get("/api/user/{userProfileId}/profile", RESIDENT_ID)
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(RESIDENT_ID.intValue()))
                .andExpect(jsonPath("$.role").value("RESIDENT"));

        System.out.println("PASSED TEST 13");
    }
}
