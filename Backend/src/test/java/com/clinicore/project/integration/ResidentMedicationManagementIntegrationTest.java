package com.clinicore.project.integration;

import com.clinicore.project.entity.Medication;
import com.clinicore.project.repository.MedicationRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Resident Medication Management — Medications tab
 * Tester: Rushabh Patel | Recorder: Sean Bombay | 04/25/2026
 *
 * Covers: Add, Edit, Update Status, Delete — by Admin and Caregiver
 * Invalid inputs: non-existent resident, non-existent medication, bad status value
 * Note: unauthorized-role enforcement is handled by the JWT authentication filter
 *       (disabled here via addFilters = false); service-layer role checks apply to reads only.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentMedicationManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Long ADMIN_ID     = 1L;   // Kevin Nguyen
    private static final Long CAREGIVER_ID = 2L;   // Emily Tran
    private static final Long RESIDENT_ID  = 4L;   // Sophia Choi

    // IDs created during the test run — passed between tests via static fields
    private static Long createdMedicationIdAdmin;
    private static Long createdMedicationIdCaregiver;

    // an existing pre-seeded medication used only for the invalid-status test
    private static Long existingMedicationId;

    @BeforeAll
    static void discoverExistingMedication(@Autowired MedicationRepository medicationRepository) {
        List<Medication> meds = medicationRepository.findByMedicalProfileResidentId(RESIDENT_ID);
        existingMedicationId = meds.isEmpty() ? null : meds.get(0).getId();
        if (existingMedicationId != null) {
            System.out.println("Using existing medication ID for invalid-status test: " + existingMedicationId);
        } else {
            System.out.println("WARNING: No pre-seeded medication found for resident " + RESIDENT_ID);
        }
    }

    // ==================== ADD MEDICATION ====================

    @Test
    @Order(1)
    @DisplayName("TEST 1: Admin adds a new medication for a resident")
    void testAdminAddsMedication() throws Exception {
        System.out.println("\n=== TEST 1: Admin Adds Medication ===");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Admin Test Med");
        body.put("dosage", "10mg");
        body.put("schedule", "Once daily");
        body.put("notes", "Added by admin integration test");
        body.put("intakeStatus", "PENDING");

        String response = mockMvc.perform(post("/api/residents/" + RESIDENT_ID + "/medications")
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Admin Test Med"))
                .andExpect(jsonPath("$.dosage").value("10mg"))
                .andExpect(jsonPath("$.schedule").value("Once daily"))
                .andExpect(jsonPath("$.intakeStatus").value("Pending"))
                .andReturn().getResponse().getContentAsString();

        createdMedicationIdAdmin = ((Number) objectMapper.readValue(response, Map.class).get("id")).longValue();
        System.out.println("PASSED TEST 1 — medicationId=" + createdMedicationIdAdmin);
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Caregiver adds a new medication for a resident")
    void testCaregiverAddsMedication() throws Exception {
        System.out.println("\n=== TEST 2: Caregiver Adds Medication ===");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Caregiver Test Med");
        body.put("dosage", "25mg");
        body.put("schedule", "Twice daily");
        body.put("notes", "Added by caregiver integration test");
        body.put("intakeStatus", "PENDING");

        String response = mockMvc.perform(post("/api/residents/" + RESIDENT_ID + "/medications")
                        .param("currentUserId", CAREGIVER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Caregiver Test Med"))
                .andExpect(jsonPath("$.dosage").value("25mg"))
                .andExpect(jsonPath("$.schedule").value("Twice daily"))
                .andExpect(jsonPath("$.intakeStatus").value("Pending"))
                .andReturn().getResponse().getContentAsString();

        createdMedicationIdCaregiver = ((Number) objectMapper.readValue(response, Map.class).get("id")).longValue();
        System.out.println("PASSED TEST 2 — medicationId=" + createdMedicationIdCaregiver);
    }

    // ==================== EDIT MEDICATION DETAILS ====================

    @Test
    @Order(3)
    @DisplayName("TEST 3: Admin edits medication dosage, schedule, and notes")
    void testAdminEditsMedicationDetails() throws Exception {
        System.out.println("\n=== TEST 3: Admin Edits Medication Details ===");

        Map<String, String> updates = new LinkedHashMap<>();
        updates.put("dosage", "20mg");
        updates.put("schedule", "Twice daily");
        updates.put("notes", "Dosage increased by admin");

        mockMvc.perform(patch("/api/residents/medications/" + createdMedicationIdAdmin)
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Medication updated successfully"));

        // verify the update was persisted
        mockMvc.perform(get("/api/user/resident/medication/dosage")
                        .param("currentUserId", ADMIN_ID.toString())
                        .param("medicationId", createdMedicationIdAdmin.toString()))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Caregiver edits medication dosage, schedule, and notes")
    void testCaregiverEditsMedicationDetails() throws Exception {
        System.out.println("\n=== TEST 4: Caregiver Edits Medication Details ===");

        Map<String, String> updates = new LinkedHashMap<>();
        updates.put("dosage", "50mg");
        updates.put("schedule", "Every 8 hours");
        updates.put("notes", "Schedule adjusted by caregiver");

        mockMvc.perform(patch("/api/residents/medications/" + createdMedicationIdCaregiver)
                        .param("currentUserId", CAREGIVER_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Medication updated successfully"));

        System.out.println("PASSED TEST 4");
    }

    // ==================== UPDATE INTAKE STATUS ====================

    @Test
    @Order(5)
    @DisplayName("TEST 5: Admin marks medication as ADMINISTERED — sets lastAdministeredAt")
    void testAdminUpdateStatusAdministered() throws Exception {
        System.out.println("\n=== TEST 5: Admin Updates Status to ADMINISTERED ===");

        mockMvc.perform(patch("/api/residents/medications/" + createdMedicationIdAdmin + "/status")
                        .param("status", "ADMINISTERED")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intakeStatus").value("Administered"))
                .andExpect(jsonPath("$.lastAdministeredAt").isNotEmpty());

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Admin marks medication as WITHHELD")
    void testAdminUpdateStatusWithheld() throws Exception {
        System.out.println("\n=== TEST 6: Admin Updates Status to WITHHELD ===");

        mockMvc.perform(patch("/api/residents/medications/" + createdMedicationIdAdmin + "/status")
                        .param("status", "WITHHELD")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intakeStatus").value("Withheld"));

        System.out.println("PASSED TEST 6");
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: Admin marks medication as MISSED")
    void testAdminUpdateStatusMissed() throws Exception {
        System.out.println("\n=== TEST 7: Admin Updates Status to MISSED ===");

        mockMvc.perform(patch("/api/residents/medications/" + createdMedicationIdAdmin + "/status")
                        .param("status", "MISSED")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intakeStatus").value("Missed"));

        System.out.println("PASSED TEST 7");
    }

    @Test
    @Order(8)
    @DisplayName("TEST 8: Admin resets medication status to PENDING")
    void testAdminUpdateStatusPending() throws Exception {
        System.out.println("\n=== TEST 8: Admin Updates Status to PENDING ===");

        mockMvc.perform(patch("/api/residents/medications/" + createdMedicationIdAdmin + "/status")
                        .param("status", "PENDING")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intakeStatus").value("Pending"));

        System.out.println("PASSED TEST 8");
    }

    // ==================== DELETE MEDICATION ====================

    @Test
    @Order(9)
    @DisplayName("TEST 9: Admin deletes a medication")
    void testAdminDeletesMedication() throws Exception {
        System.out.println("\n=== TEST 9: Admin Deletes Medication ===");

        mockMvc.perform(delete("/api/residents/medications/" + createdMedicationIdAdmin)
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Medication deleted successfully"));

        System.out.println("PASSED TEST 9");
    }

    @Test
    @Order(10)
    @DisplayName("TEST 10: Caregiver deletes a medication")
    void testCaregiverDeletesMedication() throws Exception {
        System.out.println("\n=== TEST 10: Caregiver Deletes Medication ===");

        mockMvc.perform(delete("/api/residents/medications/" + createdMedicationIdCaregiver)
                        .param("currentUserId", CAREGIVER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Medication deleted successfully"));

        System.out.println("PASSED TEST 10");
    }

    // ==================== INVALID INPUTS ====================

    @Test
    @Order(11)
    @DisplayName("TEST 11: Create medication for non-existent resident returns 400")
    void testCreateMedicationNonExistentResident() throws Exception {
        System.out.println("\n=== TEST 11: Create Medication — Non-Existent Resident ===");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "Ghost Med");
        body.put("dosage", "5mg");
        body.put("schedule", "Once daily");

        mockMvc.perform(post("/api/residents/999999/medications")
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        System.out.println("PASSED TEST 11");
    }

    @Test
    @Order(12)
    @DisplayName("TEST 12: Edit non-existent medication returns 400")
    void testEditNonExistentMedication() throws Exception {
        System.out.println("\n=== TEST 12: Edit Medication — Non-Existent Medication ID ===");

        Map<String, String> updates = new LinkedHashMap<>();
        updates.put("dosage", "99mg");

        mockMvc.perform(patch("/api/residents/medications/999999")
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isBadRequest());

        System.out.println("PASSED TEST 12");
    }

    @Test
    @Order(13)
    @DisplayName("TEST 13: Update status with invalid value returns 400")
    void testUpdateStatusInvalidValue() throws Exception {
        System.out.println("\n=== TEST 13: Update Status — Invalid Status Value ===");

        if (existingMedicationId == null) {
            System.out.println("SKIPPED TEST 13: No pre-seeded medication available");
            return;
        }

        // medication exists but "FLYING" is not a valid IntakeStatus enum value
        mockMvc.perform(patch("/api/residents/medications/" + existingMedicationId + "/status")
                        .param("status", "FLYING")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isBadRequest());

        System.out.println("PASSED TEST 13");
    }
}