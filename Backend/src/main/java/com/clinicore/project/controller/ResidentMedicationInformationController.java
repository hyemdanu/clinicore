package com.clinicore.project.controller;

import com.clinicore.project.dto.MedicationDTO;
import com.clinicore.project.service.ResidentMedicationInformationService;
import com.clinicore.project.entity.Medication;
import com.clinicore.project.entity.Medication.IntakeStatus;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class ResidentMedicationInformationController {

    private final ResidentMedicationInformationService residentMedicationInformationService;

    public ResidentMedicationInformationController(ResidentMedicationInformationService residentMedicationInformationService) {
        this.residentMedicationInformationService = residentMedicationInformationService;
    }
    
    @GetMapping("/residents/medication/list")
    public ResponseEntity<?> getResidentMedication(@RequestParam Long currentUserId, @RequestParam Long residentId) {
        try {

            List<MedicationDTO> medication = residentMedicationInformationService.getAllMedication(currentUserId, residentId);

            return ResponseEntity.ok(medication);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving resident's medication: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/medications/name")
    public ResponseEntity<?> getResidentMedicationName(@RequestParam Long currentUserId, @RequestParam Long medicationId) {
        try {

            String medicationName = residentMedicationInformationService.getName(currentUserId, medicationId);

            return ResponseEntity.ok(medicationName);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medication name: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/medication/dosage")
    public ResponseEntity<?> getResidentMedicationDosage(@RequestParam Long currentUserId, @RequestParam Long medicationId) {
        try {

            String medicationDosage = residentMedicationInformationService.getDosage(currentUserId, medicationId);

            return ResponseEntity.ok(medicationDosage);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medication dosage: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/medication/frequency")
    public ResponseEntity<?> getResidentMedicationFrequency(@RequestParam Long currentUserId, @RequestParam Long medicationId) {
        try {

            String medicationFrequency = residentMedicationInformationService.getFrequency(currentUserId, medicationId);

            return ResponseEntity.ok(medicationFrequency);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medication frequency: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/medication/intakestatus")
    public ResponseEntity<?> getResidentMedicationIntakeStatus(@RequestParam Long currentUserId, @RequestParam Long medicationId) {
        try {

            IntakeStatus medicationIntakeStatus = residentMedicationInformationService.getIntakeStatus(currentUserId, medicationId);

            return ResponseEntity.ok(medicationIntakeStatus);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medication intake status: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/medication/lastadministered")
    public ResponseEntity<?> getResidentMedicationLastAdministered(@RequestParam Long currentUserId, @RequestParam Long medicationId) {
        try {

            LocalDateTime medicationLastAdministered = residentMedicationInformationService.getLastAdministered(currentUserId, medicationId);

            return ResponseEntity.ok(medicationLastAdministered);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medication last administered status: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/medication/notes")
    public ResponseEntity<?> getResidentMedicationNotes(@RequestParam Long currentUserId, @RequestParam Long medicationId) {
        try {

            String notes = residentMedicationInformationService.getNotes(currentUserId, medicationId);

            return ResponseEntity.ok(notes);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medication notes: " + e.getMessage(), currentUserId);
        }
    }


    // standard error response
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Long userId) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message, "userId", userId));
    }
}
