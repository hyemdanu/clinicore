package com.clinicore.project.integration;

// ============================================================================
// IMPORTS SECTION
// ============================================================================
// These imports bring in the classes and methods we need for testing

// Import your entity classes (the Java objects that represent database tables)
import com.clinicore.project.entity.Resident;           // REPLACE: Change "Resident" to your entity name
import com.clinicore.project.entity.UserProfile;        // Keep this if your entity links to UserProfile

// Import your repository interfaces (these handle database operations)
import com.clinicore.project.repository.ResidentGeneralRepository;  // REPLACE: Change to your repository name
import com.clinicore.project.repository.UserProfileRepository;      // Keep this if your entity links to UserProfile

// JUnit testing framework imports - these provide testing annotations and assertion methods
import org.junit.jupiter.api.*;                         // Provides @Test, @Order, @DisplayName, etc.
import org.springframework.beans.factory.annotation.Autowired;  // Allows Spring to inject dependencies
import org.springframework.boot.test.context.SpringBootTest;    // Loads the full Spring application for testing

// Java utilities for working with dates and optional values
import java.time.LocalDate;                             // For date fields (birthday, hire date, etc.)
import java.util.Optional;                              // Wraps results that might be null

// JUnit assertion methods - these verify that your code works correctly
import static org.junit.jupiter.api.Assertions.*;       // Provides assertEquals, assertTrue, assertFalse, etc.

// ============================================================================
// JAVADOC DOCUMENTATION
// ============================================================================
/**
 * Integration Test Template for Entity and Repository
 *
 * PURPOSE:
 * This test verifies that your entity (database table) and repository
 * (database operations) work correctly with a real database.
 *
 * WHAT IT TESTS:
 * - CREATE: Can we insert new records into the database?
 * - READ: Can we retrieve records from the database?
 * - DELETE: Can we remove records from the database?
 * - VERIFY: Is the database cleaned up after tests?
 *
 * HOW TO ADAPT THIS TEMPLATE:
 * 1. Copy this entire file
 * 2. Rename file: "EntityNameIntegrationTest.java" (replace EntityName with your entity)
 * 3. Find all "REPLACE:" comments and follow instructions
 * 4. Update test data to match your entity's fields
 * 5. Update test IDs to avoid conflicts (e.g., use 888888L, 777777L, etc.)
 *
 * EXAMPLE:
 * If testing a "Caregiver" entity:
 * - Replace "Resident" with "Caregiver"
 * - Replace "ResidentGeneralRepository" with "CaregiverRepository"
 * - Update fields: emergencyContactName → specialization, etc.
 */
@SpringBootTest  // This tells Spring to start the application for testing
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)  // Run tests in order (1, 2, 3, 4)
class ResidentIntegrationTest {  // REPLACE: Change "Resident" to your entity name

    // ============================================================================
    // DEPENDENCY INJECTION
    // ============================================================================
    // @Autowired tells Spring to automatically provide these objects
    // Think of it as Spring "injecting" the repository into your test

    @Autowired
    private ResidentGeneralRepository residentRepository;  // REPLACE: Change to your repository

    @Autowired
    private UserProfileRepository userProfileRepository;   // KEEP: If your entity links to UserProfile
    // REMOVE: If your entity is standalone

    // ============================================================================
    // TEST DATA IDs
    // ============================================================================
    // We use static variables to share IDs across all test methods
    // "static" means these variables belong to the class, not individual test runs
    // Use HIGH numbers (like 999999) to avoid conflicts with real database records

    private static Long testUserProfileId = 999999L;  // REPLACE: Change number if testing different entity
    private static Long testResidentId = 999999L;     // REPLACE: Change "Resident" to your entity name

    // ============================================================================
    // TEST 1: CREATE - Insert New Records into Database
    // ============================================================================
    @Test  // Marks this method as a test that JUnit should run
    @Order(1)  // This test runs FIRST (before tests 2, 3, 4)
    @DisplayName("1. CREATE - Create UserProfile and Resident")  // Human-readable test name
    void testCreateResident() {  // REPLACE: Change "Resident" to your entity name

        // Print a message so we can see test progress in the console
        System.out.println("\n=== TEST 1: Creating UserProfile and Resident ===");

        // -----------------------------------------------------------------------
        // STEP 1: Create a UserProfile (if your entity requires one)
        // -----------------------------------------------------------------------
        // UserProfile stores basic user information (name, birthday, etc.)
        // If your entity doesn't need UserProfile, SKIP THIS SECTION

        UserProfile userProfile = new UserProfile();  // Create a new, empty UserProfile object

        // Set the ID manually (our database doesn't have AUTO_INCREMENT)
        userProfile.setId(testUserProfileId);

        // Set all required fields for UserProfile
        userProfile.setFirstName("John");           // REPLACE: Change test data as needed
        userProfile.setLastName("Doe");             // REPLACE: Change test data as needed
        userProfile.setGender("Male");              // REPLACE: Change test data as needed
        userProfile.setBirthday(LocalDate.of(1950, 5, 15));  // Format: (year, month, day)
        userProfile.setContactNumber("555-0100");

        // Generate unique username using current time in milliseconds
        // This prevents duplicate username errors if tests run multiple times
        userProfile.setUsername("johndoe_test_" + System.currentTimeMillis());
        userProfile.setPasswordHash("hashed_password_123");

        // Save to database and get back the saved object (with database-assigned values)
        UserProfile savedUserProfile = userProfileRepository.save(userProfile);

        // -----------------------------------------------------------------------
        // VERIFY: Check that UserProfile was saved successfully
        // -----------------------------------------------------------------------
        System.out.println("✓ Created UserProfile with ID: " + savedUserProfile.getId());

        // assertNotNull checks that the ID is not null (meaning save was successful)
        assertNotNull(savedUserProfile.getId(), "UserProfile ID should not be null");

        // assertEquals checks that the saved ID matches what we set
        assertEquals(testUserProfileId, savedUserProfile.getId());

        // -----------------------------------------------------------------------
        // STEP 2: Create your Entity (Resident, Caregiver, etc.)
        // -----------------------------------------------------------------------
        Resident resident = new Resident();  // REPLACE: Change "Resident" to your entity name

        // Set the ID manually (our database doesn't have AUTO_INCREMENT)
        resident.setId(testResidentId);  // REPLACE: Change "Resident" to your entity name

        // Set all required fields for your entity
        // REPLACE ALL BELOW: Update these to match YOUR entity's fields
        resident.setEmergencyContactName("Jane Doe");
        resident.setEmergencyContactNumber("555-0200");
        resident.setMedicalProfileId(null);  // Set to NULL to avoid foreign key errors
        resident.setNotes("Test resident - automated test");

        // Save to database
        Resident savedResident = residentRepository.save(resident);  // REPLACE: Change "Resident"

        // -----------------------------------------------------------------------
        // VERIFY: Check that Entity was saved successfully
        // -----------------------------------------------------------------------
        System.out.println("✓ Created Resident with ID: " + savedResident.getId());

        // Verify ID is not null
        assertNotNull(savedResident.getId(), "Resident ID should not be null");

        // Verify ID matches what we set
        assertEquals(testResidentId, savedResident.getId());

        // Verify specific field value
        // REPLACE: Update to test one of YOUR entity's important fields
        assertEquals("Jane Doe", savedResident.getEmergencyContactName());
    }

    // ============================================================================
    // TEST 2: READ - Retrieve Records from Database
    // ============================================================================
    @Test
    @Order(2)  // This test runs SECOND (after test 1 creates the data)
    @DisplayName("2. READ - Retrieve the created Resident")  // REPLACE: Change "Resident"
    void testGetResident() {  // REPLACE: Change "Resident" to your entity name

        System.out.println("\n=== TEST 2: Retrieving Resident ===");

        // -----------------------------------------------------------------------
        // Attempt to find the entity by ID
        // -----------------------------------------------------------------------
        // findById returns an Optional<Entity> which might contain the entity or be empty
        Optional<Resident> foundResident = residentRepository.findById(testResidentId);  // REPLACE

        // -----------------------------------------------------------------------
        // VERIFY: Check that entity was found
        // -----------------------------------------------------------------------
        // isPresent() returns true if the Optional contains a value
        assertTrue(foundResident.isPresent(), "Resident should be found");
        System.out.println("✓ Found Resident with ID: " + foundResident.get().getId());

        // Extract the actual entity from the Optional
        Resident resident = foundResident.get();  // REPLACE: Change "Resident"

        // Verify the field values match what we created in Test 1
        // REPLACE ALL BELOW: Update to match YOUR entity's fields
        assertEquals("Jane Doe", resident.getEmergencyContactName());
        assertEquals("555-0200", resident.getEmergencyContactNumber());

        // Print field values for visual verification
        System.out.println("✓ Emergency Contact: " + resident.getEmergencyContactName());
        System.out.println("✓ Emergency Number: " + resident.getEmergencyContactNumber());
    }

    // ============================================================================
    // TEST 3: DELETE - Remove Records from Database
    // ============================================================================
    @Test
    @Order(3)  // This test runs THIRD (after we've verified the data exists)
    @DisplayName("3. DELETE - Remove the test Resident and UserProfile")  // REPLACE
    void testDeleteResident() {  // REPLACE: Change "Resident" to your entity name

        System.out.println("\n=== TEST 3: Deleting Test Data ===");

        // -----------------------------------------------------------------------
        // Delete the Entity FIRST (to avoid foreign key constraint errors)
        // -----------------------------------------------------------------------
        residentRepository.deleteById(testResidentId);  // REPLACE: Change repository and ID

        // Try to find the deleted entity
        Optional<Resident> deletedResident = residentRepository.findById(testResidentId);  // REPLACE

        // Verify it's gone (isPresent() should return false)
        assertFalse(deletedResident.isPresent(), "Resident should be deleted");
        System.out.println("✓ Deleted Resident with ID: " + testResidentId);

        // -----------------------------------------------------------------------
        // Delete the UserProfile SECOND (after entity is deleted)
        // -----------------------------------------------------------------------
        // REMOVE THIS SECTION if your entity doesn't use UserProfile
        userProfileRepository.deleteById(testUserProfileId);

        Optional<UserProfile> deletedUserProfile = userProfileRepository.findById(testUserProfileId);
        assertFalse(deletedUserProfile.isPresent(), "UserProfile should be deleted");
        System.out.println("✓ Deleted UserProfile with ID: " + testUserProfileId);

        System.out.println("✓ Cleanup complete!");
    }

    // ============================================================================
    // TEST 4: VERIFY - Confirm Database is Clean
    // ============================================================================
    @Test
    @Order(4)  // This test runs LAST (final verification)
    @DisplayName("4. VERIFY - Confirm database is clean")
    void testDatabaseIsClean() {

        System.out.println("\n=== TEST 4: Verifying Database Cleanup ===");

        // -----------------------------------------------------------------------
        // Double-check that entities don't exist anymore
        // -----------------------------------------------------------------------
        Optional<Resident> shouldNotExist = residentRepository.findById(testResidentId);  // REPLACE
        assertFalse(shouldNotExist.isPresent(), "Test Resident should not exist");

        // REMOVE BELOW if your entity doesn't use UserProfile
        Optional<UserProfile> shouldNotExistProfile = userProfileRepository.findById(testUserProfileId);
        assertFalse(shouldNotExistProfile.isPresent(), "Test UserProfile should not exist");

        System.out.println("✓ Database is clean - no test data remains");
    }
}