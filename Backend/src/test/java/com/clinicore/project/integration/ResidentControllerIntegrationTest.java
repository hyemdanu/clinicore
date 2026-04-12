package com.clinicore.project.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ResidentController
 * Hits real endpoints, real service layer, real database
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // seeded user IDs — adjust if your seed data uses different IDs
    private static final Long ADMIN_ID = 1L;
    private static final Long RESIDENT_ID = 4L;

    // track IDs created during tests so later tests can clean up or reference them
    private static Long createdMedicationId;

    // ==================== LIST TESTS ====================

    @Test
    @Order(1)
    @DisplayName("TEST 1: GET /list returns a list of residents (lightweight)")
    void testGetResidentList() throws Exception {
        System.out.println("\n=== TEST 1: Get Resident List ===");

        mockMvc.perform(get("/api/residents/list")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: GET /medication-summary returns per-resident medication counts")
    void testGetMedicationSummary() throws Exception {
        System.out.println("\n=== TEST 2: Get Medication Summary ===");

        mockMvc.perform(get("/api/residents/medication-summary")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 2");
    }

    // ==================== FULL DETAIL TESTS ====================

    @Test
    @Order(3)
    @DisplayName("TEST 3: GET /full returns all residents with hydrated details (JOIN FETCH)")
    void testGetAllResidentsFull() throws Exception {
        System.out.println("\n=== TEST 3: Get All Residents Full ===");

        mockMvc.perform(get("/api/residents/full")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: GET /full/{id} returns one resident with medical info, medications, allergies")
    void testGetResidentByIdFull() throws Exception {
        System.out.println("\n=== TEST 4: Get Resident By ID Full ===");

        mockMvc.perform(get("/api/residents/full/" + RESIDENT_ID)
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(RESIDENT_ID))
                .andExpect(jsonPath("$.firstName").exists())
                .andExpect(jsonPath("$.lastName").exists())
                // these should be populated by the JOIN FETCH queries
                .andExpect(jsonPath("$.medications").isArray())
                .andExpect(jsonPath("$.medicalProfile").exists());

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: GET /full/{nonExistent} returns 404")
    void testGetNonExistentResident() throws Exception {
        System.out.println("\n=== TEST 5: Get Non-Existent Resident ===");

        mockMvc.perform(get("/api/residents/full/999999")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isNotFound());

        System.out.println("PASSED TEST 5");
    }

    // ==================== MEDICATION CRUD ====================

    @Test
    @Order(6)
    @DisplayName("TEST 6: POST /{residentId}/medications creates a new medication")
    void testCreateMedication() throws Exception {
        System.out.println("\n=== TEST 6: Create Medication ===");

        Map<String, Object> medication = new LinkedHashMap<>();
        medication.put("name", "Integration Test Med");
        medication.put("dosage", "50mg");
        medication.put("schedule", "Twice daily");
        medication.put("notes", "created by integration test");
        medication.put("intakeStatus", "PENDING");

        String response = mockMvc.perform(post("/api/residents/" + RESIDENT_ID + "/medications")
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(medication)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Med"))
                .andExpect(jsonPath("$.dosage").value("50mg"))
                .andReturn().getResponse().getContentAsString();

        // stash the ID so we can update/delete it later
        Map<?, ?> parsed = objectMapper.readValue(response, Map.class);
        createdMedicationId = ((Number) parsed.get("id")).longValue();

        System.out.println("PASSED TEST 6 — medicationId=" + createdMedicationId);
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: PATCH /medications/{id}/status updates medication status")
    void testUpdateMedicationStatus() throws Exception {
        System.out.println("\n=== TEST 7: Update Medication Status ===");

        mockMvc.perform(patch("/api/residents/medications/" + createdMedicationId + "/status")
                        .param("status", "ADMINISTERED")
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intakeStatus").value("Administered"));

        System.out.println("PASSED TEST 7");
    }

    @Test
    @Order(8)
    @DisplayName("TEST 8: DELETE /medications/{id} removes the medication")
    void testDeleteMedication() throws Exception {
        System.out.println("\n=== TEST 8: Delete Medication ===");

        mockMvc.perform(delete("/api/residents/medications/" + createdMedicationId)
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 8");
    }

    // ==================== MEDICAL PROFILE ====================

    @Test
    @Order(9)
    @DisplayName("TEST 9: PATCH /{residentId}/medical-profile updates insurance/notes")
    void testUpdateMedicalProfile() throws Exception {
        System.out.println("\n=== TEST 9: Update Medical Profile ===");

        // grab current insurance so we can restore it after
        String before = mockMvc.perform(get("/api/residents/full/" + RESIDENT_ID)
                        .param("currentUserId", ADMIN_ID.toString()))
                .andReturn().getResponse().getContentAsString();
        String originalInsurance = objectMapper.readTree(before)
                .path("medicalProfile").path("insurance").asText("");

        // update to test value
        Map<String, String> updates = new LinkedHashMap<>();
        updates.put("insurance", "Blue Shield Test");

        mockMvc.perform(patch("/api/residents/" + RESIDENT_ID + "/medical-profile")
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());

        // verify it took
        mockMvc.perform(get("/api/residents/full/" + RESIDENT_ID)
                        .param("currentUserId", ADMIN_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.medicalProfile.insurance").value("Blue Shield Test"));

        // restore original value
        Map<String, String> restore = new LinkedHashMap<>();
        restore.put("insurance", originalInsurance);

        mockMvc.perform(patch("/api/residents/" + RESIDENT_ID + "/medical-profile")
                        .param("currentUserId", ADMIN_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(restore)))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 9");
    }

    // ==================== AVAILABLE MEDICATIONS ====================

    @Test
    @Order(10)
    @DisplayName("TEST 10: GET /medications/available returns medication inventory list")
    void testGetAvailableMedications() throws Exception {
        System.out.println("\n=== TEST 10: Get Available Medications ===");

        mockMvc.perform(get("/api/residents/medications/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        System.out.println("PASSED TEST 10");
    }
}
