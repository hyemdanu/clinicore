package com.clinicore.project.integration;

import com.clinicore.project.controller.ResidentController;
import com.clinicore.project.dto.ResidentFullDTO;
import com.clinicore.project.service.JwtService;
import com.clinicore.project.service.ResidentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResidentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ResidentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResidentService residentService;

    @MockBean
    private JwtService jwtService;

    private static final Long ADMIN_ID = 1L;
    private static final Long RESIDENT_ID = 4L;
    private static final Long NON_EXISTENT_RESIDENT_ID = 999999L;

    @Test
    @DisplayName("GET all residents returns 200 and a list")
    void getAllResidentsReturnsList() throws Exception {
        List<Map<String, Object>> residents = List.of(
                Map.of("id", RESIDENT_ID, "firstName", "Sean", "lastName", "Bombay"),
                Map.of("id", 5L, "firstName", "Maya", "lastName", "Rivera")
        );

        when(residentService.getAllResidentsBasic()).thenReturn(residents);

        mockMvc.perform(get("/api/residents/list")
                        .param("currentUserId", String.valueOf(ADMIN_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(RESIDENT_ID))
                .andExpect(jsonPath("$[0].firstName").value("Sean"));
    }

    @Test
    @DisplayName("GET resident by id returns hydrated resident with nested medical info and profile")
    void getResidentByIdReturnsHydratedResident() throws Exception {
        when(residentService.getResidentFullDetailsById(RESIDENT_ID)).thenReturn(buildResident());

        mockMvc.perform(get("/api/residents/full/{residentId}", RESIDENT_ID)
                        .param("currentUserId", String.valueOf(ADMIN_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(RESIDENT_ID))
                .andExpect(jsonPath("$.firstName").value("Sean"))
                .andExpect(jsonPath("$.medicalProfile.insurance").value("Blue Shield"))
                .andExpect(jsonPath("$.medicalRecord.allergies[0]").value("Penicillin"))
                .andExpect(jsonPath("$.assignedCaregivers[0].caregiverId").value(2))
                .andExpect(jsonPath("$.medications[0].name").value("Aspirin"));
    }

    @Test
    @DisplayName("GET non-existent resident returns 404")
    void getNonExistentResidentReturns404() throws Exception {
        when(residentService.getResidentFullDetailsById(NON_EXISTENT_RESIDENT_ID))
                .thenThrow(new RuntimeException("Resident not found"));

        mockMvc.perform(get("/api/residents/full/{residentId}", NON_EXISTENT_RESIDENT_ID)
                        .param("currentUserId", String.valueOf(ADMIN_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resident not found"))
                .andExpect(jsonPath("$.userId").value(ADMIN_ID));
    }

    @Test
    @DisplayName("GET all residents with full details returns 200 and hydrated list")
    void getAllResidentsWithFullDetailsReturnsHydratedList() throws Exception {
        when(residentService.getAllResidentsWithFullDetails())
                .thenReturn(List.of(buildResident()));

        mockMvc.perform(get("/api/residents/full")
                        .param("currentUserId", String.valueOf(ADMIN_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(RESIDENT_ID))
                .andExpect(jsonPath("$[0].medicalProfile.insurance").value("Blue Shield"))
                .andExpect(jsonPath("$[0].medicalRecord.diagnoses[0]").value("Hypertension"));
    }

    private ResidentFullDTO buildResident() {
        return new ResidentFullDTO(
                RESIDENT_ID,
                "resident@example.com",
                "Sean",
                "Bombay",
                "Male",
                LocalDate.of(1947, 6, 12),
                "555-111-2222",
                "Anita Bombay",
                "555-333-4444",
                "Requires evening wellness check.",
                List.of(new ResidentFullDTO.AssignedCaregiverDTO(2L, "Emily Tran")),
                new ResidentFullDTO.MedicalProfileDTO("Blue Shield", "Diabetic"),
                new ResidentFullDTO.MedicalServicesDTO(
                        "Comfort Care",
                        "General Hospital",
                        "CVS",
                        "Home Health Inc.",
                        "Peaceful Rest",
                        "DNR on file",
                        true,
                        true
                ),
                new ResidentFullDTO.CapabilityDTO(true, false, "Assisted", "Walker"),
                new ResidentFullDTO.MedicalRecordDTO(
                        List.of("Penicillin"),
                        List.of("Hypertension"),
                        "Monitor blood pressure weekly.",
                        List.of(new ResidentFullDTO.AllergyDTO(10L, RESIDENT_ID, "Penicillin", 3, "Rash")),
                        List.of(new ResidentFullDTO.DiagnosisDTO(20L, RESIDENT_ID, "Hypertension", "Stable"))
                ),
                List.of(new ResidentFullDTO.MedicationDTO(
                        30L,
                        40L,
                        "Aspirin",
                        "81mg",
                        "Daily",
                        12,
                        "Take with food",
                        "PENDING",
                        null,
                        "2026-04-12T18:00:00",
                        false
                ))
        );
    }
}
