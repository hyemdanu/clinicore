package com.clinicore.project.integration;

// ============================================================================
// IMPORTS SECTION
// ============================================================================

// Import your entity classes
import com.clinicore.project.entity.Resident;           // REPLACE: Change to your entity
import com.clinicore.project.entity.UserProfile;        // Keep if your entity links to UserProfile

// Import your repository interfaces
import com.clinicore.project.repository.ResidentGeneralRepository;  // REPLACE
import com.clinicore.project.repository.UserProfileRepository;      // Keep if needed

// JUnit testing framework
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;  // Enables MockMvc
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;               // For specifying JSON content type
import org.springframework.test.web.servlet.MockMvc;     // Simulates HTTP requests

// Java utilities
import java.time.LocalDate;

// Hamcrest matchers - for flexible string matching in JSON responses
import static org.hamcrest.Matchers.*;

// Spring MockMvc methods - for building HTTP requests and checking responses
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  // get, post, put, delete
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;    // status, jsonPath

// ============================================================================
// JAVADOC DOCUMENTATION
// ============================================================================
/**
 * Controller Integration Test Template
 *
 * PURPOSE:
 * This test verifies that your REST API controller endpoints work correctly
 * by making actual HTTP requests and checking the responses.
 *
 * WHAT IT TESTS:
 * - Setup: Creates test data in the database
 * - GET Success: Tests retrieving data via API (200 OK response)
 * - GET Not Found: Tests API error handling (404 Not Found response)
 * - Cleanup: Removes test data from database
 *
 * KEY CONCEPTS:
 * - MockMvc: Simulates HTTP requests without starting a real web server
 * - JSON: Data format used by REST APIs (like {"id": 1, "name": "John"})
 * - jsonPath: Way to navigate and test JSON response structure
 * - HTTP Status Codes: 200 (OK), 404 (Not Found), 500 (Server Error), etc.
 *
 * HOW TO ADAPT THIS TEMPLATE:
 * 1. Copy this entire file
 * 2. Rename file: "EntityNameControllerIntegrationTest.java"
 * 3. Find all "REPLACE:" comments and follow instructions
 * 4. Update API endpoints to match your controller's @GetMapping, @PostMapping, etc.
 * 5. Update jsonPath expressions to match your API's JSON response structure
 * 6. Update test IDs to avoid conflicts (e.g., use 777777L)
 *
 * EXAMPLE:
 * If testing a CaregiverController with endpoint "/api/caregiver/{id}":
 * - Replace "Resident" with "Caregiver"
 * - Replace "/api/resident/" with "/api/caregiver/"
 * - Update jsonPath checks to match Caregiver fields
 */
@SpringBootTest  // Loads the full Spring application
@AutoConfigureMockMvc(addFilters = false)  // Enables MockMvc and disables Spring Security for testing
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // Run tests in order
class ResidentControllerIntegrationTest {  // REPLACE: Change "Resident" to your entity name

    // ============================================================================
    // DEPENDENCY INJECTION
    // ============================================================================

    @Autowired
    private MockMvc mockMvc;  // This object simulates HTTP requests to your API
                              // Think of it as a fake web browser for testing

    @Autowired
    private ResidentGeneralRepository residentRepository;  // REPLACE: Your repository

    @Autowired
    private UserProfileRepository userProfileRepository;   // Keep if your entity uses UserProfile

    // ============================================================================
    // TEST DATA IDs
    // ============================================================================
    // Use DIFFERENT IDs than your repository tests to avoid conflicts
    // For example: Repository tests use 999999L, Controller tests use 888888L

    private static Long testUserProfileId = 888888L;  // REPLACE: Change number for different entity
    private static Long testResidentId = 888888L;     // REPLACE: Change number for different entity

    // ============================================================================
    // TEST 1: SETUP - Create Test Data in Database
    // ============================================================================
    @Test
    @Order(1)  // Runs FIRST to create data before API tests
    @DisplayName("Setup - Create test data in database")
    void setupTestData() {

        System.out.println("\n=== SETUP: Creating test data ===");

        // -----------------------------------------------------------------------
        // Create a UserProfile (if your entity requires one)
        // -----------------------------------------------------------------------
        // REMOVE THIS SECTION if your entity doesn't use UserProfile

        UserProfile userProfile = new UserProfile();
        userProfile.setId(testUserProfileId);
        userProfile.setFirstName("Jane");           // REPLACE: Change test data
        userProfile.setLastName("Smith");           // REPLACE: Change test data
        userProfile.setGender("Female");            // REPLACE: Change test data
        userProfile.setBirthday(LocalDate.of(1960, 3, 20));
        userProfile.setContactNumber("555-1234");
        userProfile.setUsername("janesmith_test_" + System.currentTimeMillis());
        userProfile.setPasswordHash("hashed_password_456");
        userProfileRepository.save(userProfile);

        // -----------------------------------------------------------------------
        // Create your Entity
        // -----------------------------------------------------------------------
        Resident resident = new Resident();  // REPLACE: Change "Resident" to your entity
        resident.setId(testResidentId);

        // REPLACE ALL BELOW: Set fields that match YOUR entity
        resident.setEmergencyContactName("John Smith");
        resident.setEmergencyContactNumber("555-5678");
        resident.setMedicalProfileId(null);
        resident.setNotes("Controller test resident");

        residentRepository.save(resident);  // REPLACE: Change repository

        System.out.println("✓ Test data created (UserProfile ID: " + testUserProfileId +
                         ", Resident ID: " + testResidentId + ")");
    }

    // ============================================================================
    // TEST 2: GET SUCCESS - Test API Returns Data Successfully
    // ============================================================================
    @Test
    @Order(2)
    @DisplayName("GET /api/resident/{id}/general - Success (200)")  // REPLACE: Update endpoint
    void testGetResidentGeneral_Success() throws Exception {  // REPLACE: Rename method

        System.out.println("\n=== TEST: GET /api/resident/{id}/general (Success) ===");

        // -----------------------------------------------------------------------
        // Make HTTP GET request to your API endpoint
        // -----------------------------------------------------------------------
        mockMvc.perform(
                // Build a GET request to your API endpoint
                // REPLACE: Change "/api/resident/{userProfileId}/general" to YOUR endpoint
                get("/api/resident/{userProfileId}/general", testUserProfileId)
                        .contentType(MediaType.APPLICATION_JSON)  // Tell API we're working with JSON
        )
        // -----------------------------------------------------------------------
        // VERIFY: Check the HTTP response
        // -----------------------------------------------------------------------
        .andExpect(status().isOk())  // Expect HTTP 200 OK status code

        // -----------------------------------------------------------------------
        // VERIFY: Check the JSON response body
        // -----------------------------------------------------------------------
        // jsonPath checks specific fields in the JSON response
        // Syntax: "$.fieldName" means "find field called 'fieldName' in the JSON"
        // REPLACE ALL BELOW: Update jsonPath checks to match YOUR API's JSON structure

        .andExpect(jsonPath("$.userProfileId").value(testUserProfileId))
        // This checks: {"userProfileId": 888888, ...}

        .andExpect(jsonPath("$.username").value(containsString("janesmith_test")))
        // containsString() does partial matching (useful for generated usernames with timestamps)

        .andExpect(jsonPath("$.gender").value("Female"))
        .andExpect(jsonPath("$.birthday").value("1960-03-20"))
        .andExpect(jsonPath("$.contactNumber").value("555-1234"))
        .andExpect(jsonPath("$.emergencyContactName").value("John Smith"))
        .andExpect(jsonPath("$.emergencyContactNumber").value("555-5678"))
        .andExpect(jsonPath("$.notes").value("Controller test resident"));

        System.out.println("✓ GET request successful - returned correct resident data");
    }

    // ============================================================================
    // TEST 3: GET NOT FOUND - Test API Error Handling (UserProfile Missing)
    // ============================================================================
    @Test
    @Order(3)
    @DisplayName("GET /api/resident/{id}/general - UserProfile Not Found (404)")  // REPLACE
    void testGetResidentGeneral_UserNotFound() throws Exception {  // REPLACE: Rename method

        System.out.println("\n=== TEST: GET /api/resident/{id}/general (User Not Found) ===");

        // Use an ID that definitely doesn't exist in the database
        Long nonExistentId = 999999999L;

        // -----------------------------------------------------------------------
        // Make HTTP GET request with non-existent ID
        // -----------------------------------------------------------------------
        mockMvc.perform(
                get("/api/resident/{userProfileId}/general", nonExistentId)  // REPLACE: Update endpoint
                        .contentType(MediaType.APPLICATION_JSON)
        )
        // -----------------------------------------------------------------------
        // VERIFY: Check for HTTP 404 Not Found
        // -----------------------------------------------------------------------
        .andExpect(status().isNotFound())  // Expect HTTP 404 status code

        // -----------------------------------------------------------------------
        // VERIFY: Check error message in JSON response
        // -----------------------------------------------------------------------
        // REPLACE BELOW: Update to match YOUR controller's error response format
        .andExpect(jsonPath("$.message").value("UserProfile not found"))
        .andExpect(jsonPath("$.userProfileId").value(nonExistentId));

        System.out.println("✓ Correctly returns 404 for non-existent user");
    }

    // ============================================================================
    // TEST 4: GET NOT FOUND - Test API Error Handling (Entity Missing)
    // ============================================================================
    @Test
    @Order(4)
    @DisplayName("GET /api/resident/{id}/general - Resident Not Found (404)")  // REPLACE
    void testGetResidentGeneral_ResidentNotFound() throws Exception {  // REPLACE: Rename method

        System.out.println("\n=== TEST: GET /api/resident/{id}/general (Resident Not Found) ===");

        // -----------------------------------------------------------------------
        // Create a UserProfile WITHOUT a corresponding Entity
        // -----------------------------------------------------------------------
        // This tests what happens when UserProfile exists but Entity doesn't

        Long orphanUserId = 777777L;  // Use yet another unique ID
        UserProfile orphanUser = new UserProfile();
        orphanUser.setId(orphanUserId);
        orphanUser.setFirstName("Orphan");
        orphanUser.setLastName("User");
        orphanUser.setGender("Other");
        orphanUser.setBirthday(LocalDate.of(1980, 1, 1));
        orphanUser.setContactNumber("555-0000");
        orphanUser.setUsername("orphan_test_" + System.currentTimeMillis());
        orphanUser.setPasswordHash("hashed_password_789");
        userProfileRepository.save(orphanUser);

        // -----------------------------------------------------------------------
        // Make HTTP GET request for UserProfile without Entity
        // -----------------------------------------------------------------------
        mockMvc.perform(
                get("/api/resident/{userProfileId}/general", orphanUserId)  // REPLACE: Update endpoint
                        .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound())  // Expect 404

        // REPLACE BELOW: Update to match YOUR controller's error response
        .andExpect(jsonPath("$.message").value("Resident not found for userProfileId"))
        .andExpect(jsonPath("$.userProfileId").value(orphanUserId));

        // Cleanup orphan user
        userProfileRepository.deleteById(orphanUserId);

        System.out.println("✓ Correctly returns 404 when resident data is missing");
    }

    // ============================================================================
    // TEST 5: CLEANUP - Remove All Test Data
    // ============================================================================
    @Test
    @Order(5)  // Runs LAST to clean up after all tests
    @DisplayName("Cleanup - Remove all test data from database")
    void cleanupTestData() {

        System.out.println("\n=== CLEANUP: Removing test data ===");

        // Delete Entity first (to avoid foreign key errors)
        residentRepository.deleteById(testResidentId);  // REPLACE: Change repository and ID

        // Delete UserProfile second
        // REMOVE THIS if your entity doesn't use UserProfile
        userProfileRepository.deleteById(testUserProfileId);

        System.out.println("✓ Test data cleaned up");
    }
}