package com.clinicore.project.integration;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ResidentMedicationInformationController
 * Tests role-based access control for medication information
 * Covers: Medication list and individual medication details
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentMedicationInformationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Test User IDs
    private static final Long RESIDENT_ID = 4L;      // Sophia Choi
    private static final Long CAREGIVER_ID = 2L;     // Emily Tran
    private static final Long ADMIN_ID = 1L;         // Kevin Nguyen

    // Medication ID for testing (will be set dynamically)
    private static Long testMedicationId;

    @BeforeAll
    static void setupTestData(@Autowired MedicationRepository medicationRepository) {
        // Find or create a test medication for resident 4
        List<Medication> medications = medicationRepository.findByMedicalProfileResidentId(RESIDENT_ID);
        if (!medications.isEmpty()) {
            testMedicationId = medications.get(0).getId();
            System.out.println("Using existing medication ID: " + testMedicationId);
        } else {
            System.out.println("WARNING: No medication found for resident " + RESIDENT_ID);
            testMedicationId = 1L; // Fallback to medication ID 1
        }
    }

    // ==================== RESIDENT TESTS ====================

    @Test
    @Order(1)
    @DisplayName("TEST 1: Resident should read own medication list")
    void testResidentReadOwnMedicationList() throws Exception {
        System.out.println("\n=== TEST 1: Resident Reading Own Medication List ===");

        mockMvc.perform(get("/api/user/residents/medication/list")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Resident should read own medication name")
    void testResidentReadOwnMedicationName() throws Exception {
        System.out.println("\n=== TEST 2: Resident Reading Own Medication Name ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/medications/name")
                            .param("currentUserId", String.valueOf(RESIDENT_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(String.class)));

            System.out.println("PASSED TEST 2");
        } else {
            System.out.println("SKIPPED TEST 2: No medication available");
        }
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Resident should read own medication dosage")
    void testResidentReadOwnMedicationDosage() throws Exception {
        System.out.println("\n=== TEST 3: Resident Reading Own Medication Dosage ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/dosage")
                            .param("currentUserId", String.valueOf(RESIDENT_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 3");
        } else {
            System.out.println("SKIPPED TEST 3: No medication available");
        }
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Resident should read own medication frequency")
    void testResidentReadOwnMedicationFrequency() throws Exception {
        System.out.println("\n=== TEST 4: Resident Reading Own Medication Frequency ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/frequency")
                            .param("currentUserId", String.valueOf(RESIDENT_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 4");
        } else {
            System.out.println("SKIPPED TEST 4: No medication available");
        }
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Resident should read own medication intake status")
    void testResidentReadOwnMedicationIntakeStatus() throws Exception {
        System.out.println("\n=== TEST 5: Resident Reading Own Medication Intake Status ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/intakestatus")
                            .param("currentUserId", String.valueOf(RESIDENT_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 5");
        } else {
            System.out.println("SKIPPED TEST 5: No medication available");
        }
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Resident should read own medication last administered")
    void testResidentReadOwnMedicationLastAdministered() throws Exception {
        System.out.println("\n=== TEST 6: Resident Reading Own Medication Last Administered ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/lastadministered")
                            .param("currentUserId", String.valueOf(RESIDENT_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 6");
        } else {
            System.out.println("SKIPPED TEST 6: No medication available");
        }
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: Resident should read own medication notes")
    void testResidentReadOwnMedicationNotes() throws Exception {
        System.out.println("\n=== TEST 7: Resident Reading Own Medication Notes ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/notes")
                            .param("currentUserId", String.valueOf(RESIDENT_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 7");
        } else {
            System.out.println("SKIPPED TEST 7: No medication available");
        }
    }

    // ==================== CAREGIVER TESTS ====================

    @Test
    @Order(8)
    @DisplayName("TEST 8: Caregiver should read resident's medication list")
    void testCaregiverReadResidentMedicationList() throws Exception {
        System.out.println("\n=== TEST 8: Caregiver Reading Resident's Medication List ===");

        mockMvc.perform(get("/api/user/residents/medication/list")
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 8");
    }

    @Test
    @Order(9)
    @DisplayName("TEST 9: Caregiver should read resident's medication name")
    void testCaregiverReadResidentMedicationName() throws Exception {
        System.out.println("\n=== TEST 9: Caregiver Reading Resident's Medication Name ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/medications/name")
                            .param("currentUserId", String.valueOf(CAREGIVER_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(String.class)));

            System.out.println("PASSED TEST 9");
        } else {
            System.out.println("SKIPPED TEST 9: No medication available");
        }
    }

    @Test
    @Order(10)
    @DisplayName("TEST 10: Caregiver should read resident's medication dosage")
    void testCaregiverReadResidentMedicationDosage() throws Exception {
        System.out.println("\n=== TEST 10: Caregiver Reading Resident's Medication Dosage ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/dosage")
                            .param("currentUserId", String.valueOf(CAREGIVER_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 10");
        } else {
            System.out.println("SKIPPED TEST 10: No medication available");
        }
    }

    @Test
    @Order(11)
    @DisplayName("TEST 11: Caregiver should read resident's medication frequency")
    void testCaregiverReadResidentMedicationFrequency() throws Exception {
        System.out.println("\n=== TEST 11: Caregiver Reading Resident's Medication Frequency ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/frequency")
                            .param("currentUserId", String.valueOf(CAREGIVER_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 11");
        } else {
            System.out.println("SKIPPED TEST 11: No medication available");
        }
    }

    @Test
    @Order(12)
    @DisplayName("TEST 12: Caregiver should read resident's medication intake status")
    void testCaregiverReadResidentMedicationIntakeStatus() throws Exception {
        System.out.println("\n=== TEST 12: Caregiver Reading Resident's Medication Intake Status ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/intakestatus")
                            .param("currentUserId", String.valueOf(CAREGIVER_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 12");
        } else {
            System.out.println("SKIPPED TEST 12: No medication available");
        }
    }

    // ==================== ADMIN TESTS ====================

    @Test
    @Order(13)
    @DisplayName("TEST 13: Admin should read resident's medication list")
    void testAdminReadResidentMedicationList() throws Exception {
        System.out.println("\n=== TEST 13: Admin Reading Resident's Medication List ===");

        mockMvc.perform(get("/api/user/residents/medication/list")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 13");
    }

    @Test
    @Order(14)
    @DisplayName("TEST 14: Admin should read resident's medication name")
    void testAdminReadResidentMedicationName() throws Exception {
        System.out.println("\n=== TEST 14: Admin Reading Resident's Medication Name ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/medications/name")
                            .param("currentUserId", String.valueOf(ADMIN_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", isA(String.class)));

            System.out.println("PASSED TEST 14");
        } else {
            System.out.println("SKIPPED TEST 14: No medication available");
        }
    }

    @Test
    @Order(15)
    @DisplayName("TEST 15: Admin should read resident's medication dosage")
    void testAdminReadResidentMedicationDosage() throws Exception {
        System.out.println("\n=== TEST 15: Admin Reading Resident's Medication Dosage ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/dosage")
                            .param("currentUserId", String.valueOf(ADMIN_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 15");
        } else {
            System.out.println("SKIPPED TEST 15: No medication available");
        }
    }

    @Test
    @Order(16)
    @DisplayName("TEST 16: Admin should read resident's medication notes")
    void testAdminReadResidentMedicationNotes() throws Exception {
        System.out.println("\n=== TEST 16: Admin Reading Resident's Medication Notes ===");

        if (testMedicationId != null) {
            mockMvc.perform(get("/api/user/resident/medication/notes")
                            .param("currentUserId", String.valueOf(ADMIN_ID))
                            .param("medicationId", String.valueOf(testMedicationId))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            System.out.println("PASSED TEST 16");
        } else {
            System.out.println("SKIPPED TEST 16: No medication available");
        }
    }
}
