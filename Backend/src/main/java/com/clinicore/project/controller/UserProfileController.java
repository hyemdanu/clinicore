/**
 * Olei Amelie Ngan & Edison Ho (refactored our tasks into one module for consolidation)
 * Clinicore Project
 *
 * Controller for user profile (Caregiver, Resident, Admin) information
 *
 * Functions/Purposes:
 *  - Retrieve user profile information with role-based access
 *  - Retrieve list of residents for caregivers/admins
 *
 *  Role-based access control
 *  - Residents can only view their own information
 *  - Caregivers/Admins can view a list of residents and selected resident's information
 *  - Admins can view all user information (c/a/r)
 *
 *  Side note: this is not an authentication controller
 */

package com.clinicore.project.controller;

import com.clinicore.project.service.UserProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserProfileController {

    private final UserProfileService userProfileService;

    // Inject the service layer
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * Get a list of all residents (for caregiver and admin ONLY)
     */
    @GetMapping("/residents/list")
    public ResponseEntity<?> getResidentsList(@RequestParam Long currentUserId) {
        try {
            List<Map<String, Object>> residents = userProfileService.getAllResidents(currentUserId);
            return ResponseEntity.ok(residents);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving residents: " + e.getMessage(), currentUserId);
        }
    }

    /**
     * Get user profile with role-based authorization
     */
    @GetMapping("/{userProfileId}/profile")
    public ResponseEntity<?> getProfileData(@PathVariable Long userProfileId, 
                                           @RequestParam Long currentUserId) {
        try {
            Map<String, Object> profileResponse = userProfileService.getUserProfile(currentUserId, userProfileId);
            return ResponseEntity.ok(profileResponse);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving profile: " + e.getMessage(), currentUserId);
        }
    }

    /**
     * Create standardized error response
     */
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Long userId) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message, "userId", userId));
    }
}
