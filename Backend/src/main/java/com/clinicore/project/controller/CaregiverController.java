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
}
