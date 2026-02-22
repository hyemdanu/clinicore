package com.clinicore.project.controller;

import com.clinicore.project.service.CaregiverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/caregivers")
@CrossOrigin
public class CaregiverController {

    private final CaregiverService caregiverService;

    public CaregiverController(CaregiverService caregiverService) {
        this.caregiverService = caregiverService;
    }

    // returns the full caregiver list, each one with their residents attached
    @GetMapping
    public ResponseEntity<?> getCaregivers(@RequestParam Long currentUserId) {
        try {
            List<Map<String, Object>> caregivers = caregiverService.getAllCaregivers(currentUserId);
            return ResponseEntity.ok(caregivers);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving caregivers: " + e.getMessage()));
        }
    }

    // returns all residents (id, firstName, lastName) for dropdowns
    @GetMapping("/residents")
    public ResponseEntity<?> getAllResidents(@RequestParam Long currentUserId) {
        try {
            return ResponseEntity.ok(caregiverService.getAllResidents(currentUserId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving residents: " + e.getMessage()));
        }
    }

    // assign a resident to a caregiver
    @PostMapping("/{caregiverId}/residents/{residentId}")
    public ResponseEntity<?> assignResident(@PathVariable Long caregiverId,
                                            @PathVariable Long residentId,
                                            @RequestParam Long currentUserId) {
        try {
            caregiverService.assignResident(caregiverId, residentId, currentUserId);
            return ResponseEntity.ok(Map.of("message", "Resident assigned successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error assigning resident: " + e.getMessage()));
        }
    }

    // remove a resident from a caregiver
    @DeleteMapping("/{caregiverId}/residents/{residentId}")
    public ResponseEntity<?> removeResident(@PathVariable Long caregiverId,
                                            @PathVariable Long residentId,
                                            @RequestParam Long currentUserId) {
        try {
            caregiverService.removeResident(caregiverId, residentId, currentUserId);
            return ResponseEntity.ok(Map.of("message", "Resident removed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error removing resident: " + e.getMessage()));
        }
    }

    // switch a resident from this caregiver to another
    @PutMapping("/{caregiverId}/residents/{residentId}/switch")
    public ResponseEntity<?> switchResident(@PathVariable Long caregiverId,
                                            @PathVariable Long residentId,
                                            @RequestParam Long toCaregiverId,
                                            @RequestParam Long currentUserId) {
        try {
            caregiverService.switchResident(caregiverId, residentId, toCaregiverId, currentUserId);
            return ResponseEntity.ok(Map.of("message", "Resident switched successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error switching resident: " + e.getMessage()));
        }
    }

    // returns residents split into assigned to this caregiver vs everyone else
    @GetMapping("/my-residents")
    public ResponseEntity<?> getMyResidents(@RequestParam Long currentUserId) {
        try {
            return ResponseEntity.ok(caregiverService.getResidentsSplitForCaregiver(currentUserId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error retrieving residents: " + e.getMessage()));
        }
    }
}
