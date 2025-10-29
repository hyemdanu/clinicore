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

    // MockMvc is used to simulate a HTTP request to the server
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResidentGeneralRepository residentRepository;

    private static final Long RESIDENT_ID = 4L;

    @Test
    @Order(1)
    @DisplayName("1. READ - Get resident details via endpoint")
    void getResident1Details() throws Exception {
        System.out.println("\n=== TEST 1: Reading Resident Details ===");

        mockMvc.perform(get("/api/resident/{id}/general", RESIDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())

                // These are the expected values
                .andExpect(jsonPath("$.userProfileId").value(RESIDENT_ID.intValue()))
                .andExpect(jsonPath("$.firstName").value("Sophia"))
                .andExpect(jsonPath("$.lastName").value("Choi"))
                .andExpect(jsonPath("$.gender").value("Female"))
                .andExpect(jsonPath("$.emergencyContactName").value("Jane Done"))
                .andExpect(jsonPath("$.emergencyContactNumber").value("555-9999"));

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("2. UPDATE - Update resident emergency contact")
    void updateResident1EmergencyContact() {
        System.out.println("\n=== TEST 2: Update Resident Emergency Contact ===");

        // Find resident
        Resident resident = residentRepository.findById(RESIDENT_ID).orElse(null);
        assertNotNull(resident, "Resident should exist");

        // Original emergency contact details
        String originalName = resident.getEmergencyContactName();

        // Update emergency contact
        resident.setEmergencyContactName("New Jane Done");
        resident.setEmergencyContactNumber("000-0000");

        Resident updatedResident = residentRepository.save(resident);

        // Verify that the emergency contact was updated
        assertEquals("New Jane Done", updatedResident.getEmergencyContactName());
        assertEquals("000-0000", updatedResident.getEmergencyContactNumber());
        assertNotEquals(originalName, updatedResident.getEmergencyContactName());

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("3. READ - Verify resident was updated via endpoint")
    void verifyUpdatedResident1() throws Exception {
        System.out.println("\n=== TEST 4: Verifying Updated Resident 1 ===");

        mockMvc.perform(get("/api/resident/{id}/general", RESIDENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emergencyContactName").value("New Jane Done"))
                .andExpect(jsonPath("$.emergencyContactNumber").value("000-0000"));

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("4. READ - List all residents")
    void listAllResidents() throws Exception {
        System.out.println("\n=== TEST 4: Listing All Residents ===");

        mockMvc.perform(get("/api/resident/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))

                // Just check first three residents
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[?(@.firstName == 'Sophia')]").exists())
                .andExpect(jsonPath("$[?(@.firstName == 'Aaron')]").exists())
                .andExpect(jsonPath("$[?(@.firstName == 'Jessica')]").exists());

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("5. READ - Non-existent resident (error handling)")
    void getResidentNotFound() throws Exception {
        System.out.println("\n=== TEST 6: Reading Non-Existent Resident ===");

        Long nonExistentId = 99999L;

        mockMvc.perform(get("/api/resident/{id}/general", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());

        System.out.println("PASSED TEST 5");
    }

}
