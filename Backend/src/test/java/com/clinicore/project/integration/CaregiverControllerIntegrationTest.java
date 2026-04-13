package com.clinicore.project.integration;

import com.clinicore.project.entity.ResidentCaregiverId;
import com.clinicore.project.repository.ResidentCaregiverRepository;
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
 * Integration tests for CaregiverController.
 * Tests list access, authorization, JOIN FETCH batch-load, and assignment persistence.
 *
 * Note: CaregiverController has no GET-by-id or PUT-fields endpoint.
 * Tests 2 and 5 are adapted accordingly:
 *   - Test 2 verifies a specific caregiver's data within the full list response.
 *   - Test 5 uses the assign → verify → remove round-trip to prove write persistence.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CaregiverControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResidentCaregiverRepository residentCaregiverRepository;

    private static final Long ADMIN_ID     = 1L;   // Kevin Nguyen  (ADMIN)
    private static final Long CAREGIVER_ID = 2L;   // Emily Tran    (CAREGIVER)
    private static final Long RESIDENT_ID  = 4L;   // Sophia Choi   (RESIDENT)

    // ==================== TEST 1: GET ALL CAREGIVERS ====================

    @Test
    @Order(1)
    @DisplayName("TEST 1: Admin gets all caregivers — 200 + non-empty list with expected fields")
    void testGetAllCaregiversAsAdmin() throws Exception {
        System.out.println("\n=== TEST 1: GET All Caregivers (Admin) ===");

        mockMvc.perform(get("/api/caregivers")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].firstName").exists())
                .andExpect(jsonPath("$[0].lastName").exists())
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[0].residents").isArray());

        System.out.println("PASSED TEST 1");
    }

    // ==================== TEST 2: SPECIFIC CAREGIVER IN LIST ====================

    @Test
    @Order(2)
    @DisplayName("TEST 2: Specific caregiver (Emily Tran, id=2) is present with correct data")
    void testSpecificCaregiverInList() throws Exception {
        System.out.println("\n=== TEST 2: Find Specific Caregiver in List ===");

        mockMvc.perform(get("/api/caregivers")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 2)].firstName", contains("Emily")))
                .andExpect(jsonPath("$[?(@.id == 2)].lastName", contains("Tran")))
                .andExpect(jsonPath("$[?(@.id == 2)].email").exists())
                .andExpect(jsonPath("$[?(@.id == 2)].residents").isArray());

        System.out.println("PASSED TEST 2");
    }

    // ==================== TEST 3: NON-EXISTENT USER → 403 ====================

    @Test
    @Order(3)
    @DisplayName("TEST 3: Non-existent currentUserId returns 403 Forbidden")
    void testGetCaregiversNonExistentUser() throws Exception {
        System.out.println("\n=== TEST 3: GET Caregivers — Non-existent User (id=9999) ===");

        mockMvc.perform(get("/api/caregivers")
                        .param("currentUserId", "9999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("9999")));

        System.out.println("PASSED TEST 3");
    }

    // ==================== TEST 4: MY-RESIDENTS — JOIN FETCH BATCH LOAD ====================

    @Test
    @Order(4)
    @DisplayName("TEST 4: Caregiver gets split residents list — validates JOIN FETCH batch load")
    void testGetMyResidentsHydratedList() throws Exception {
        System.out.println("\n=== TEST 4: GET My Residents (hydrated via JOIN FETCH) ===");

        mockMvc.perform(get("/api/caregivers/my-residents")
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigned").isArray())
                .andExpect(jsonPath("$.others").isArray())
                // every entry in 'others' carries the assignedCaregivers list (hydrated from JOIN FETCH)
                .andExpect(jsonPath("$.others[*].id").exists())
                .andExpect(jsonPath("$.others[*].firstName").exists())
                .andExpect(jsonPath("$.others[*].assignedCaregivers").exists());

        System.out.println("PASSED TEST 4");
    }

    // ==================== TEST 5: ASSIGNMENT PERSISTENCE ROUND-TRIP ====================

    @Test
    @Order(5)
    @DisplayName("TEST 5: Assign resident to caregiver persists in DB, then remove cleans up")
    void testAssignResidentPersistsAndRemove() throws Exception {
        System.out.println("\n=== TEST 5: Assign → Verify → Remove (Persistence Round-Trip) ===");

        // Ensure clean state — remove pre-existing assignment so POST doesn't return 400
        ResidentCaregiverId key = new ResidentCaregiverId(RESIDENT_ID, CAREGIVER_ID);
        if (residentCaregiverRepository.existsById(key)) {
            residentCaregiverRepository.deleteById(key);
        }

        // Step 1: Assign resident 4 to caregiver 2
        mockMvc.perform(post("/api/caregivers/{caregiverId}/residents/{residentId}",
                        CAREGIVER_ID, RESIDENT_ID)
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resident assigned successfully"));

        // Step 2: Verify assignment is persisted — resident 4 must appear under caregiver 2
        mockMvc.perform(get("/api/caregivers")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 2)].residents[*].id",
                        hasItem(RESIDENT_ID.intValue())));

        // Step 3: Remove assignment (cleanup + tests the DELETE endpoint)
        mockMvc.perform(delete("/api/caregivers/{caregiverId}/residents/{residentId}",
                        CAREGIVER_ID, RESIDENT_ID)
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Resident removed successfully"));

        System.out.println("PASSED TEST 5");
    }
}
