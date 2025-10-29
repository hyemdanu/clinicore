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
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for ResidentGeneralController
 * Tests: Create resident → Get resident details → List all residents → Delete resident
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResidentGeneralRepository residentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private static Long testUserId;

    @Test
    @Order(1)
    @DisplayName("1. CREATE - Make new resident in database")
    void createResident() {
        System.out.println("\n=== TEST 1: Creating Resident ===");

        // Create UserProfile (don't set ID - let it be auto-generated)
        UserProfile user = new UserProfile();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setGender("Male");
        user.setBirthday(LocalDate.of(1950, 5, 15));
        user.setContactNumber("555-1234");
        user.setUsername("johndoe_test_" + System.currentTimeMillis());
        user.setPasswordHash("hashed_password");
        
        // Save and capture the generated ID
        UserProfile savedUser = userProfileRepository.save(user);
        testUserId = savedUser.getId();

        // Verify UserProfile ID was generated
        assertNotNull(testUserId, "User ID should be generated");
        assertTrue(testUserId > 0, "User ID should be positive");

        // Create Resident with the same ID as UserProfile
        Resident resident = new Resident();
        resident.setId(testUserId);
        resident.setEmergencyContactName("Jane Doe");
        resident.setEmergencyContactNumber("555-5678");
        resident.setMedicalProfileId(null);
        resident.setNotes("Test resident");
        residentRepository.save(resident);

        System.out.println("✓ Created resident with auto-generated ID: " + testUserId);
    }

    @Test
    @Order(2)
    @DisplayName("2. GET - Grab resident information via endpoint")
    void getResidentInformation() throws Exception {
        System.out.println("\n=== TEST 2: Getting Resident Information ===");

        mockMvc.perform(get("/api/resident/{id}/general", testUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userProfileId").value(testUserId))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.emergencyContactName").value("Jane Doe"))
                .andExpect(jsonPath("$.emergencyContactNumber").value("555-5678"))
                .andExpect(jsonPath("$.notes").value("Test resident"));

        System.out.println("✓ Retrieved resident information successfully with ID: " + testUserId);
    }

    @Test
    @Order(3)
    @DisplayName("3. GET LIST - List all residents via endpoint")
    void listAllResidents() throws Exception {
        System.out.println("\n=== TEST 3: Listing All Residents ===");

        mockMvc.perform(get("/api/resident/list")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$[?(@.id == " + testUserId + ")].firstName").value(hasItem("John")))
                .andExpect(jsonPath("$[?(@.id == " + testUserId + ")].lastName").value(hasItem("Doe")));

        System.out.println("✓ Listed all residents successfully");
    }

    @Test
    @Order(4)
    @DisplayName("4. DELETE - Delete resident from database")
    void deleteResident() {
        System.out.println("\n=== TEST 4: Deleting Resident ===");

        residentRepository.deleteById(testUserId);
        userProfileRepository.deleteById(testUserId);
        
        // Verify deletion
        assertFalse(residentRepository.existsById(testUserId), "Resident should be deleted");
        assertFalse(userProfileRepository.existsById(testUserId), "UserProfile should be deleted");

        System.out.println("✓ Deleted resident with ID: " + testUserId);
    }
}
