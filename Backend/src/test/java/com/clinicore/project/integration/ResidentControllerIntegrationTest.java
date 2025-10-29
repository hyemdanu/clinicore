package com.clinicore.project.integration;

import com.clinicore.project.entity.Resident;
import com.clinicore.project.repository.ResidentGeneralRepository;
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
 * Integration test for ResidentGeneralController
 * Tests: Read residents → Update resident details
 * Uses pre-populated database with 5 residents
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResidentGeneralRepository residentRepository;

    private static final Long RESIDENT_ID_1 = 4L;  // John Doe
    private static final Long RESIDENT_ID_2 = 5L;  // Mary Johnson
    private static final String UPDATED_EMERGENCY_NAME = "Updated Contact Name";
    private static final String UPDATED_EMERGENCY_NUMBER = "555-9999";

    @Test
    @Order(1)
    @DisplayName("1. READ - Get resident 1 details via endpoint")
    void getResident1Details() throws Exception {
        System.out.println("\n=== TEST 1: Reading Resident 1 Details ===");

        mockMvc.perform(get("/api/resident/{id}/general", RESIDENT_ID_1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(RESIDENT_ID_1.intValue()))
                .andExpect(jsonPath("$.firstName").value("Sophia"))
                .andExpect(jsonPath("$.lastName").value("Choi"))
                .andExpect(jsonPath("$.gender").value("Female"))
                .andExpect(jsonPath("$.emergencyContactName").value("Jane Doe"))
                .andExpect(jsonPath("$.emergencyContactNumber").value("555-9999"));

        System.out.println("✓ Successfully retrieved resident 1 details");
    }

    @Test
    @Order(2)
    @DisplayName("2. READ - Get resident 2 details via endpoint")
    void getResident2Details() throws Exception {
        System.out.println("\n=== TEST 2: Reading Resident 2 Details ===");

        mockMvc.perform(get("/api/resident/{id}/general", RESIDENT_ID_2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(RESIDENT_ID_2.intValue()))
                .andExpect(jsonPath("$.firstName").value("Aaron"))
                .andExpect(jsonPath("$.lastName").value("Kim"))
                .andExpect(jsonPath("$.emergencyContactName").value("Tom Johnson"));

        System.out.println("✓ Successfully retrieved resident 2 details");
    }

    @Test
    @Order(3)
    @DisplayName("3. UPDATE - Update resident 1 emergency contact")
    void updateResident1EmergencyContact() {
        System.out.println("\n=== TEST 3: Updating Resident 1 Emergency Contact ===");

        Resident resident = residentRepository.findById(RESIDENT_ID_1).orElse(null);
        assertNotNull(resident, "Resident should exist");
        
        String originalName = resident.getEmergencyContactName();

        // Update emergency contact
        resident.setEmergencyContactName(UPDATED_EMERGENCY_NAME);
        resident.setEmergencyContactNumber(UPDATED_EMERGENCY_NUMBER);

        Resident updatedResident = residentRepository.save(resident);

        assertEquals(UPDATED_EMERGENCY_NAME, updatedResident.getEmergencyContactName());
        assertEquals(UPDATED_EMERGENCY_NUMBER, updatedResident.getEmergencyContactNumber());
        assertNotEquals(originalName, updatedResident.getEmergencyContactName());

        System.out.println("✓ Successfully updated resident 1");
    }

    @Test
    @Order(4)
    @DisplayName("4. READ - Verify resident 1 was updated via endpoint")
    void verifyUpdatedResident1() throws Exception {
        System.out.println("\n=== TEST 4: Verifying Updated Resident 1 ===");

        mockMvc.perform(get("/api/resident/{id}/general", RESIDENT_ID_1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emergencyContactName").value(UPDATED_EMERGENCY_NAME))
                .andExpect(jsonPath("$.emergencyContactNumber").value(UPDATED_EMERGENCY_NUMBER));

        System.out.println("✓ Verified resident 1 update via endpoint");
    }

    @Test
    @Order(5)
    @DisplayName("5. READ - List all residents")
    void listAllResidents() throws Exception {
        System.out.println("\n=== TEST 5: Listing All Residents ===");

        mockMvc.perform(get("/api/resident/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[?(@.firstName == 'Sophia')]").exists())
                .andExpect(jsonPath("$[?(@.firstName == 'Aaron')]").exists())
                .andExpect(jsonPath("$[?(@.firstName == 'Jessica')]").exists());

        System.out.println("✓ Successfully listed all 5 residents");
    }

    @Test
    @Order(6)
    @DisplayName("6. READ - Get non-existent resident (error handling)")
    void getResidentNotFound() throws Exception {
        System.out.println("\n=== TEST 6: Reading Non-Existent Resident ===");

        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/resident/{id}/general", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("✓ Correctly returned 404 for non-existent resident");
    }

    @Test
    @Order(7)
    @DisplayName("7. UPDATE - Update resident 2 notes")
    void updateResident2Notes() {
        System.out.println("\n=== TEST 7: Updating Resident 2 Notes ===");

        Resident resident = residentRepository.findById(RESIDENT_ID_2).orElse(null);
        assertNotNull(resident, "Resident should exist");

        String newNotes = "Updated: Diabetic, requires special diet and daily monitoring";
        resident.setNotes(newNotes);

        Resident updatedResident = residentRepository.save(resident);
        assertEquals(newNotes, updatedResident.getNotes());

        System.out.println("✓ Successfully updated resident 2 notes");
    }

    @Test
    @Order(8)
    @DisplayName("8. READ - Verify resident 2 notes were updated")
    void verifyUpdatedResident2Notes() throws Exception {
        System.out.println("\n=== TEST 8: Verifying Updated Resident 2 Notes ===");

        mockMvc.perform(get("/api/resident/{id}/general", RESIDENT_ID_2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes").value(containsString("Updated")))
                .andExpect(jsonPath("$.notes").value(containsString("daily monitoring")));

        System.out.println("✓ Verified resident 2 notes update");
    }
}
