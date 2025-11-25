package com.clinicore.project.controller;

import com.clinicore.project.dto.MedicationInventoryDTO;
import com.clinicore.project.dto.ResidentFullDTO;
import com.clinicore.project.service.ResidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller to handle resident endpoints
 * handles information like their medical profile, medications, allergies, diagnoses, etc.
 */
@RestController
@RequestMapping("/api/residents")
public class ResidentController {

    @Autowired
    private ResidentService residentService;


    // error logging for this controller
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Long userId) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message, "userId", userId != null ? userId : 0));
    }

    /**
     * GET /api/residents/full
     * this will return all residents with their information like:
     * - User profile data
     * - Emergency contacts
     * - Medical profile
     * - Medical services
     * - Capability
     * - Medical records (allergies, diagnoses)
     * - Medications
     */
    @GetMapping("/full")
    public ResponseEntity<?> getAllResidentsWithFullDetails(@RequestParam Long currentUserId) {
        try {
            List<ResidentFullDTO> residents = residentService.getAllResidentsWithFullDetails();
            return ResponseEntity.ok(residents);
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error retrieving residents: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * GET /api/residents/full/{residentId}
     * returns info on one resident from their ID
     */
    @GetMapping("/full/{residentId}")
    public ResponseEntity<?> getResidentFullDetails(
            @PathVariable Long residentId,
            @RequestParam Long currentUserId) {
        try {
            ResidentFullDTO resident = residentService.getResidentFullDetailsById(residentId);
            return ResponseEntity.ok(resident);
        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.NOT_FOUND,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error retrieving resident: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * POST /api/residents/{residentId}/medications
     * create new medication for resident
     */
    @PostMapping("/{residentId}/medications")
    public ResponseEntity<?> createMedication(
            @PathVariable Long residentId,
            @RequestParam Long currentUserId,
            @RequestBody ResidentFullDTO.MedicationDTO medicationDTO) {
        try {
            ResidentFullDTO.MedicationDTO createdMedication =
                residentService.createMedication(residentId, medicationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMedication);
        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error creating medication: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * PATCH /api/residents/medications/{medicationId}/status
     * updates the status of a medication (PENDING, ADMINISTERED, WITHHELD, MISSED)
     */
    @PatchMapping("/medications/{medicationId}/status")
    public ResponseEntity<?> updateMedicationStatus(
            @PathVariable Long medicationId,
            @RequestParam String status,
            @RequestParam Long currentUserId) {
        try {
            ResidentFullDTO.MedicationDTO updatedMedication =
                residentService.updateMedicationStatus(medicationId, status);
            return ResponseEntity.ok(updatedMedication);
        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error updating medication status: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * PATCH /api/residents/medications/{medicationId}
     * updates medication details
     */
    @PatchMapping("/medications/{medicationId}")
    public ResponseEntity<?> updateMedication(
            @PathVariable Long medicationId,
            @RequestParam Long currentUserId,
            @RequestBody Map<String, String> updates) {
        try {
            residentService.updateMedication(medicationId, updates);
            return ResponseEntity.ok(Map.of("message", "Medication updated successfully"));

        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error updating medication: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * DELETE /api/residents/medications/{medicationId}
     * deletes a medication
     */
    @DeleteMapping("/medications/{medicationId}")
    public ResponseEntity<?> deleteMedication(
            @PathVariable Long medicationId,
            @RequestParam Long currentUserId) {
        try {
            residentService.deleteMedication(medicationId);
            return ResponseEntity.ok(Map.of("message", "Medication deleted successfully"));

        } catch (RuntimeException e) {
            return createErrorResponse(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage(),
                    currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error deleting medication: " + e.getMessage(),
                    currentUserId
            );
        }
    }

    /**
     * GET /api/residents/medications/available
     * get all medication from inventory
     */
    @GetMapping("/medications/available")
    public ResponseEntity<?> getAvailableMedications() {
        try {
            List<MedicationInventoryDTO> medications = residentService.getAvailableMedications();
            return ResponseEntity.ok(medications);
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error retrieving medications: " + e.getMessage(),
                null
            );
        }
    }

    /**
     * POST /api/residents/{residentId}/allergies
     * create new allergy for resident
     */
    @PostMapping("/{residentId}/allergies")
    public ResponseEntity<?> createAllergy(
            @PathVariable Long residentId,
            @RequestParam Long currentUserId,
            @RequestBody ResidentFullDTO.AllergyDTO allergyDTO) {
        try {
            ResidentFullDTO.AllergyDTO createdAllergy =
                residentService.createAllergy(residentId, allergyDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAllergy);
        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error creating allergy: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * DELETE /api/residents/allergies/{allergyId}
     * deletes an allergy
     */
    @DeleteMapping("/allergies/{allergyId}")
    public ResponseEntity<?> deleteAllergy(
            @PathVariable Long allergyId,
            @RequestParam Long currentUserId) {
        try {
            residentService.deleteAllergy(allergyId);
            return ResponseEntity.ok(Map.of("message", "Allergy deleted successfully"));
        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error deleting allergy: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * POST /api/residents/{residentId}/diagnoses
     * create diagnosis for resident
     */
    @PostMapping("/{residentId}/diagnoses")
    public ResponseEntity<?> createDiagnosis(
            @PathVariable Long residentId,
            @RequestParam Long currentUserId,
            @RequestBody ResidentFullDTO.DiagnosisDTO diagnosisDTO) {
        try {
            ResidentFullDTO.DiagnosisDTO createdDiagnosis =
                residentService.createDiagnosis(residentId, diagnosisDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(createdDiagnosis);

        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error creating diagnosis: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * DELETE /api/residents/diagnoses/{diagnosisId}
     * deletes a diagnosis
     */
    @DeleteMapping("/diagnoses/{diagnosisId}")
    public ResponseEntity<?> deleteDiagnosis(
            @PathVariable Long diagnosisId,
            @RequestParam Long currentUserId) {
        try {
            residentService.deleteDiagnosis(diagnosisId);
            return ResponseEntity.ok(Map.of("message", "Diagnosis deleted successfully"));
        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error deleting diagnosis: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * PATCH /api/residents/{residentId}/medical-profile
     * updates medical profile (insurance and notes)
     */
    @PatchMapping("/{residentId}/medical-profile")
    public ResponseEntity<?> updateMedicalProfile(
            @PathVariable Long residentId,
            @RequestParam Long currentUserId,
            @RequestBody Map<String, String> updates) {
        try {
            residentService.updateMedicalProfile(residentId, updates);
            return ResponseEntity.ok(Map.of("message", "Medical profile updated successfully"));

        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error updating medical profile: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * PATCH /api/residents/{residentId}/medical-services
     * updates medical services for the resident
     */
    @PatchMapping("/{residentId}/medical-services")
    public ResponseEntity<?> updateMedicalServices(
            @PathVariable Long residentId,
            @RequestParam Long currentUserId,
            @RequestBody Map<String, Object> updates) {
        try {
            residentService.updateMedicalServices(residentId, updates);
            return ResponseEntity.ok(Map.of("message", "Medical services updated successfully"));
        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error updating medical services: " + e.getMessage(),
                currentUserId
            );
        }
    }

    /**
     * PATCH /api/residents/{residentId}/capabilities
     * updates capabilities
     */
    @PatchMapping("/{residentId}/capabilities")
    public ResponseEntity<?> updateCapabilities(
            @PathVariable Long residentId,
            @RequestParam Long currentUserId,
            @RequestBody Map<String, Object> updates) {
        try {
            residentService.updateCapabilities(residentId, updates);
            return ResponseEntity.ok(Map.of("message", "Capabilities updated successfully"));
        } catch (RuntimeException e) {
            return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                e.getMessage(),
                currentUserId
            );
        } catch (Exception e) {
            return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error updating capabilities: " + e.getMessage(),
                currentUserId
            );
        }
    }
}
