package com.clinicore.project.integration;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
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
 * Integration tests for ResidentMedicalInformationController
 * Tests role-based access control for medical information
 * Covers: Medical Profile, Medical Record, and Medical Services endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResidentMedicalInformationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    // Test User IDs
    private static final Long RESIDENT_ID = 4L;      // Sophia Choi
    private static final Long CAREGIVER_ID = 2L;     // Emily Tran
    private static final Long ADMIN_ID = 1L;         // Kevin Nguyen

    // ==================== RESIDENT TESTS ====================

    @Test
    @Order(1)
    @DisplayName("TEST 1: Resident should read own insurance")
    void testResidentReadOwnInsurance() throws Exception {
        System.out.println("\n=== TEST 1: Resident Reading Own Insurance ===");

        mockMvc.perform(get("/api/medicalInformation/resident/insurance")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 1");
    }

    @Test
    @Order(2)
    @DisplayName("TEST 2: Resident should read own medical profile notes")
    void testResidentReadOwnMedicalProfileNotes() throws Exception {
        System.out.println("\n=== TEST 2: Resident Reading Own Medical Profile Notes ===");

        mockMvc.perform(get("/api/medicalInformation/resident/medicalprofilenotes")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 2");
    }

    @Test
    @Order(3)
    @DisplayName("TEST 3: Resident should read own capability")
    void testResidentReadOwnCapability() throws Exception {
        System.out.println("\n=== TEST 3: Resident Reading Own Capability ===");

        mockMvc.perform(get("/api/medicalInformation/resident/capability")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residentId").value(RESIDENT_ID.intValue()));

        System.out.println("PASSED TEST 3");
    }

    @Test
    @Order(4)
    @DisplayName("TEST 4: Resident should read own allergies")
    void testResidentReadOwnAllergies() throws Exception {
        System.out.println("\n=== TEST 4: Resident Reading Own Allergies ===");

        mockMvc.perform(get("/api/medicalInformation/resident/allergy")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 4");
    }

    @Test
    @Order(5)
    @DisplayName("TEST 5: Resident should read own diagnoses")
    void testResidentReadOwnDiagnoses() throws Exception {
        System.out.println("\n=== TEST 5: Resident Reading Own Diagnoses ===");

        mockMvc.perform(get("/api/medicalInformation/resident/diagnosis")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 5");
    }

    @Test
    @Order(6)
    @DisplayName("TEST 6: Resident should read own medical record notes")
    void testResidentReadOwnMedicalRecordNotes() throws Exception {
        System.out.println("\n=== TEST 6: Resident Reading Own Medical Record Notes ===");

        mockMvc.perform(get("/api/medicalInformation/resident/medicalrecordnotes")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 6");
    }

    @Test
    @Order(7)
    @DisplayName("TEST 7: Resident should read own hospice agency")
    void testResidentReadOwnHospiceAgency() throws Exception {
        System.out.println("\n=== TEST 7: Resident Reading Own Hospice Agency ===");

        mockMvc.perform(get("/api/medicalInformation/resident/hospiceagency")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 7");
    }

    @Test
    @Order(8)
    @DisplayName("TEST 8: Resident should read own preferred hospital")
    void testResidentReadOwnPreferredHospital() throws Exception {
        System.out.println("\n=== TEST 8: Resident Reading Own Preferred Hospital ===");

        mockMvc.perform(get("/api/medicalInformation/resident/preferredhospital")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 8");
    }

    @Test
    @Order(9)
    @DisplayName("TEST 9: Resident should read own DNR/POLST")
    void testResidentReadOwnDNRPolst() throws Exception {
        System.out.println("\n=== TEST 9: Resident Reading Own DNR/POLST ===");

        mockMvc.perform(get("/api/medicalInformation/resident/dnrPolst")
                        .param("currentUserId", String.valueOf(RESIDENT_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 9");
    }

    // ==================== CAREGIVER TESTS ====================

    @Test
    @Order(10)
    @DisplayName("TEST 10: Caregiver should read resident's insurance")
    void testCaregiverReadResidentInsurance() throws Exception {
        System.out.println("\n=== TEST 10: Caregiver Reading Resident's Insurance ===");

        mockMvc.perform(get("/api/medicalInformation/resident/insurance")
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 10");
    }

    @Test
    @Order(11)
    @DisplayName("TEST 11: Caregiver should read resident's capability")
    void testCaregiverReadResidentCapability() throws Exception {
        System.out.println("\n=== TEST 11: Caregiver Reading Resident's Capability ===");

        mockMvc.perform(get("/api/medicalInformation/resident/capability")
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residentId").value(RESIDENT_ID.intValue()));

        System.out.println("PASSED TEST 11");
    }

    @Test
    @Order(12)
    @DisplayName("TEST 12: Caregiver should read resident's allergies")
    void testCaregiverReadResidentAllergies() throws Exception {
        System.out.println("\n=== TEST 12: Caregiver Reading Resident's Allergies ===");

        mockMvc.perform(get("/api/medicalInformation/resident/allergy")
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 12");
    }

    @Test
    @Order(13)
    @DisplayName("TEST 13: Caregiver should read resident's diagnoses")
    void testCaregiverReadResidentDiagnoses() throws Exception {
        System.out.println("\n=== TEST 13: Caregiver Reading Resident's Diagnoses ===");

        mockMvc.perform(get("/api/medicalInformation/resident/diagnosis")
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 13");
    }

    @Test
    @Order(14)
    @DisplayName("TEST 14: Caregiver should read resident's medical services")
    void testCaregiverReadResidentMedicalServices() throws Exception {
        System.out.println("\n=== TEST 14: Caregiver Reading Resident's Medical Services ===");

        mockMvc.perform(get("/api/medicalInformation/resident/hospice")
                        .param("currentUserId", String.valueOf(CAREGIVER_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 14");
    }

    // ==================== ADMIN TESTS ====================

    @Test
    @Order(15)
    @DisplayName("TEST 15: Admin should read resident's insurance")
    void testAdminReadResidentInsurance() throws Exception {
        System.out.println("\n=== TEST 15: Admin Reading Resident's Insurance ===");

        mockMvc.perform(get("/api/medicalInformation/resident/insurance")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 15");
    }

    @Test
    @Order(16)
    @DisplayName("TEST 16: Admin should read resident's capability")
    void testAdminReadResidentCapability() throws Exception {
        System.out.println("\n=== TEST 16: Admin Reading Resident's Capability ===");

        mockMvc.perform(get("/api/medicalInformation/resident/capability")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.residentId").value(RESIDENT_ID.intValue()));

        System.out.println("PASSED TEST 16");
    }

    @Test
    @Order(17)
    @DisplayName("TEST 17: Admin should read resident's allergies")
    void testAdminReadResidentAllergies() throws Exception {
        System.out.println("\n=== TEST 17: Admin Reading Resident's Allergies ===");

        mockMvc.perform(get("/api/medicalInformation/resident/allergy")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 17");
    }

    @Test
    @Order(18)
    @DisplayName("TEST 18: Admin should read resident's diagnoses")
    void testAdminReadResidentDiagnoses() throws Exception {
        System.out.println("\n=== TEST 18: Admin Reading Resident's Diagnoses ===");

        mockMvc.perform(get("/api/medicalInformation/resident/diagnosis")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));

        System.out.println("PASSED TEST 18");
    }

    @Test
    @Order(19)
    @DisplayName("TEST 19: Admin should read resident's preferred pharmacy")
    void testAdminReadResidentPreferredPharmacy() throws Exception {
        System.out.println("\n=== TEST 19: Admin Reading Resident's Preferred Pharmacy ===");

        mockMvc.perform(get("/api/medicalInformation/resident/preferredpharmacy")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 19");
    }

    @Test
    @Order(20)
    @DisplayName("TEST 20: Admin should read resident's home health status")
    void testAdminReadResidentHomeHealth() throws Exception {
        System.out.println("\n=== TEST 20: Admin Reading Resident's Home Health Status ===");

        mockMvc.perform(get("/api/medicalInformation/resident/homehealth")
                        .param("currentUserId", String.valueOf(ADMIN_ID))
                        .param("residentId", String.valueOf(RESIDENT_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        System.out.println("PASSED TEST 20");
    }
}
