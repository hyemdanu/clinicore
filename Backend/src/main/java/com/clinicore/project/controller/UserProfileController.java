// Olei Amelie Ngan
// User Profile Controller class, which is used to return information about either caregiver or admin
// Decided to centralize caregiver and admin controllers in one file since they are very similar
// Decided to separate resident from caregiver and admin for classification + more differences
// Side note: This is not an authentication controller

package com.clinicore.project.controller;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic profile controller for all user types (Admin, Caregiver, Resident)
 * Returns role-specific data based on user type
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserProfileController {

    private final UserProfileRepository userProfileRepository;

    public UserProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Get user profile with role-specific data
     * Works for Admin, Caregiver, and Resident users
     * 
     * @param userProfileId The ID of the user to retrieve
     * @return JSON object containing user profile and role-specific information
     */
    @GetMapping("/{userProfileId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userProfileId) {
        // Fetch user profile
        UserProfile up = userProfileRepository.findById(userProfileId).orElse(null);
        
        if (up == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found", "userProfileId", userProfileId));
        }

        // Build base response with common user fields
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userProfileId", up.getId());
        response.put("username", up.getUsername());
        response.put("firstName", up.getFirstName());
        response.put("lastName", up.getLastName());
        response.put("gender", up.getGender());
        response.put("birthday", up.getBirthday());
        response.put("contactNumber", up.getContactNumber());
        response.put("role", up.getRole().toString());  // ← Include role

        // Add role-specific data
        switch (up.getRole()) {
            case ADMIN:
                // Admin role - no additional fields needed
                break;
                
            case CAREGIVER:
                // Caregiver-specific data
                Caregiver caregiver = up.getCaregiver();
                if (caregiver != null) {
                    response.put("caregiverNotes", caregiver.getNotes());
                }
                break;
                
            case RESIDENT:
                // Resident-specific data
                Resident resident = up.getResident();
                if (resident != null) {
                    response.put("emergencyContactName", resident.getEmergencyContactName());
                    response.put("emergencyContactNumber", resident.getEmergencyContactNumber());
                    response.put("residentNotes", resident.getNotes());
                }
                break;
        }

        return ResponseEntity.ok(response);
    }
}



