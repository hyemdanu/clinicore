package com.clinicore.project.controller;

//import com.clinicore.project.service.MedicalInformationService;
import com.clinicore.project.entity.Capability;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/api/medicalInformation")
public class ResidentMedicalInformationController {

    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Long userId) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message, "userId", userId));
    }
    
    /*
    private final ResidentMedicalInformationService residentMedicalInformationService;

    public ResidentMedicalInformationController(ResidentMedicalInformation residentMedicalInformationService) {
        this.residentMedicalInformationService = residentMedicalInformationService;
    }

     */

    /*
     * Medical Profile Information 
     */
    @GetMapping("/resident/insurance")
    public ResponseEntity<?> getResidentInsurance(@RequestParam Long currentUserId) {
        try {

            String insurance;// = residentMedicalInformationService.getInsurance(currentUserId);
            
            return ResponseEntity.ok(insurance);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's insurance: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/medicalprofilenotes")
    public ResponseEntity<?> getResidentMedicalProfileNotes(@RequestParam Long currentUserId) {
        try {

            String notes;// = residentMedicalInformationService.getMedicalProfileNotes(currentUserId);
            
            return ResponseEntity.ok(notes);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medical profile notes: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/capability")
    public ResponseEntity<?> getResidentCapability(@RequestParam Long currentUserId) {
        try {

            Capability capability;// = residentMedicalInformationService.getCapability(currentUserId);
            
            return ResponseEntity.ok(capability);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's capability: " + e.getMessage(), currentUserId);
        }
    }

    /*
     * Medical Record Information
     */

     @GetMapping("/resident/allergy")
    public ResponseEntity<?> getResidentAllergy(@RequestParam Long currentUserId) {
        try {

            List<Map<String, Object>> allergies; //= residentMedicalInformationService.getAllAllergies(currentUserId);
            return ResponseEntity.ok(allergies);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's allergies: " + e.getMessage(), currentUserId);
        }
    }

     @GetMapping("/resident/allergy")
    public ResponseEntity<?> getResidentDiagnoses(@RequestParam Long currentUserId) {
        try {

            List<Map<String, Object>> diagnoses; //= residentMedicalInformationService.getAllDiagnoses(currentUserId);
            return ResponseEntity.ok(diagnoses);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's allergies: " + e.getMessage(), currentUserId);
        }
    }
    
    @GetMapping("/resident/medicalrecordnotes")
    public ResponseEntity<?> getResidentMedicalRecordNotes(@RequestParam Long currentUserId) {
        try {

            String notes;// = residentMedicalInformationService.getMedicalRecordNotes(currentUserId);
            
            return ResponseEntity.ok(notes);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medical profile notes: " + e.getMessage(), currentUserId);
        }
    }

    /*
     * Medical Service Information
     */

    @GetMapping("/resident/hospiceagency")
    public ResponseEntity<?> getResidentHospiceAgency(@RequestParam Long currentUserId) {
        try {

            String hospiceAgency;// = residentMedicalInformationService.getHospiceAgency(currentUserId);
            
            return ResponseEntity.ok(hospiceAgency);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's hospice agency: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/preferredhospital")
    public ResponseEntity<?> getResidentPreferredHospital(@RequestParam Long currentUserId) {
        try {

            String preferredHospital;// = residentMedicalInformationService.getPreferredHospital(currentUserId);
            
            return ResponseEntity.ok(preferredHospital);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's preferred hospital: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/preferredpharmacy")
    public ResponseEntity<?> getResidentPreferredPharmacy(@RequestParam Long currentUserId) {
        try {

            String preferredPharmacy;// = residentMedicalInformationService.getPreferredPharmacy(currentUserId);
            
            return ResponseEntity.ok(preferredPharmacy);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's preferred pharmacy: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/homehealthagency")
    public ResponseEntity<?> getResidentHomeHealthAgency(@RequestParam Long currentUserId) {
        try {

            String homeHealthAgency;// = residentMedicalInformationService.getPreferredHomeHealthAgency(currentUserId);
            
            return ResponseEntity.ok(homeHealthAgency);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's home health agency: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/mortuary")
    public ResponseEntity<?> getResidentMortuary(@RequestParam Long currentUserId) {
        try {

            String mortuary;// = residentMedicalInformationService.getMortuary(currentUserId);
            
            return ResponseEntity.ok(mortuary);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's mortuary: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/dnrPolst")
    public ResponseEntity<?> getResidentDNRPolst(@RequestParam Long currentUserId) {
        try {

            String dnrPolst;// = residentMedicalInformationService.getDNRPolst(currentUserId);
            
            return ResponseEntity.ok(dnrPolst);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's DNR Polst: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/hospice")
    public ResponseEntity<?> getResidentHospice(@RequestParam Long currentUserId) {
        try {

            Boolean hospice;// = residentMedicalInformationService.getHospice(currentUserId);
            
            return ResponseEntity.ok(hospice);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's hospice: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/homehealth")
    public ResponseEntity<?> getResidentHomeHealth(@RequestParam Long currentUserId) {
        try {

            Boolean homeHealth;// = residentMedicalInformationService.getHomeHealth(currentUserId);
            
            return ResponseEntity.ok(homeHealth);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's home health: " + e.getMessage(), currentUserId);
        }
    }

    @GetMapping("/resident/medicalservicenotes")
    public ResponseEntity<?> getResidentMedicalServicesNotes(@RequestParam Long currentUserId) {
        try {

            String notes;// = residentMedicalInformationService.getMedicalServiceNotes(currentUserId);
            
            return ResponseEntity.ok(notes);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving resident's medical service notes: " + e.getMessage(), currentUserId);
        }
    }
}
