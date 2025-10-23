package com.clinicore.project.integration;

import com.clinicore.project.entity.Resident;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.ResidentGeneralRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for Resident entity and repository
 * Tests full CRUD operations: Create, Read, Delete
 * 
 * This test is reusable - you can copy this pattern for other entities
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentIntegrationTest {

    @Autowired
    private ResidentGeneralRepository residentRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Use high numbers to avoid conflicts with existing data
    private static Long testUserProfileId = 999999L;
    private static Long testResidentId = 999999L;

    @Test
    @Order(1)
    @DisplayName("1. POST - Create UserProfile and Resident")
    void testCreateResident() {
        System.out.println("\n=== TEST 1: Creating UserProfile and Resident ===");

        // First, create a UserProfile (required for Resident)
        UserProfile userProfile = new UserProfile();
        userProfile.setId(testUserProfileId);  // Set ID manually
        userProfile.setFirstName("John");
        userProfile.setLastName("Doe");
        userProfile.setGender("Male");
        userProfile.setBirthday(LocalDate.of(1950, 5, 15));
        userProfile.setContactNumber("555-0100");
        userProfile.setUsername("johndoe_test_" + System.currentTimeMillis());
        userProfile.setPasswordHash("hashed_password_123");

        UserProfile savedUserProfile = userProfileRepository.save(userProfile);
        
        System.out.println("✓ Created UserProfile with ID: " + savedUserProfile.getId());
        assertNotNull(savedUserProfile.getId(), "UserProfile ID should not be null");
        assertEquals(testUserProfileId, savedUserProfile.getId());

        // Now create a Resident linked to this UserProfile
        Resident resident = new Resident();
        resident.setId(testResidentId);  // Set ID manually
        resident.setEmergencyContactName("Jane Doe");
        resident.setEmergencyContactNumber("555-0200");
        resident.setMedicalProfileId(null);  // Set to NULL instead of 1L
        resident.setNotes("Test resident - automated test");

        Resident savedResident = residentRepository.save(resident);

        System.out.println("✓ Created Resident with ID: " + savedResident.getId());
        assertNotNull(savedResident.getId(), "Resident ID should not be null");
        assertEquals(testResidentId, savedResident.getId());
        assertEquals("Jane Doe", savedResident.getEmergencyContactName());
    }

    @Order(2)
    @DisplayName("2. GET - Retrieve the created Resident")
    void testGetResident() {
        System.out.println("\n=== TEST 2: Retrieving Resident ===");

        Optional<Resident> foundResident = residentRepository.findById(testResidentId);

        assertTrue(foundResident.isPresent(), "Resident should be found");
        System.out.println("✓ Found Resident with ID: " + foundResident.get().getId());
        
        Resident resident = foundResident.get();
        assertEquals("Jane Doe", resident.getEmergencyContactName());
        assertEquals("555-0200", resident.getEmergencyContactNumber());
        System.out.println("✓ Emergency Contact: " + resident.getEmergencyContactName());
        System.out.println("✓ Emergency Number: " + resident.getEmergencyContactNumber());
    }

    @Test
    @Order(3)
    @DisplayName("3. DELETE - Remove the test Resident and UserProfile")
    void testDeleteResident() {
        System.out.println("\n=== TEST 3: Deleting Test Data ===");

        // Delete Resident
        residentRepository.deleteById(testResidentId);
        Optional<Resident> deletedResident = residentRepository.findById(testResidentId);
        assertFalse(deletedResident.isPresent(), "Resident should be deleted");
        System.out.println("✓ Deleted Resident with ID: " + testResidentId);

        // Delete UserProfile
        userProfileRepository.deleteById(testUserProfileId);
        Optional<UserProfile> deletedUserProfile = userProfileRepository.findById(testUserProfileId);
        assertFalse(deletedUserProfile.isPresent(), "UserProfile should be deleted");
        System.out.println("✓ Deleted UserProfile with ID: " + testUserProfileId);

        System.out.println("✓ Cleanup complete!");
    }

    @Test
    @Order(4)
    @DisplayName("4. VERIFY - Confirm database is clean")
    void testDatabaseIsClean() {
        System.out.println("\n=== TEST 4: Verifying Database Cleanup ===");

        Optional<Resident> shouldNotExist = residentRepository.findById(testResidentId);
        assertFalse(shouldNotExist.isPresent(), "Test Resident should not exist");
        
        Optional<UserProfile> shouldNotExistProfile = userProfileRepository.findById(testUserProfileId);
        assertFalse(shouldNotExistProfile.isPresent(), "Test UserProfile should not exist");
        
        System.out.println("✓ Database is clean - no test data remains");
    }
}