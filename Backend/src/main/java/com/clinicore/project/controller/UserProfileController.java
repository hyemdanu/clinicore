/**
 * Olei Amelie Ngan & Edison Ho (refactored our tasks into one module for consolidation)
 *
 * Controller for user profiles (Caregiver, Resident, Admin) to retrieve user information + list all residents
 * It uses the user profile service layer to check role-based access
 *
 *  Role-based access
 *  - Residents can only view their own information
 *  - Caregivers/Admins can view list of residents and selected resident's information
 *  - Admins can view all user information (c/a/r) and list of residents (need to add list of caregivers later on...)
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

    // service layers to be injected
    private final UserProfileService userProfileService;

    // inject the service layers (service layers hold business logic - controller just forwards requests to service layers for logic checks)
    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    // get list of residents for caregivers/admins only
    @GetMapping("/residents/list")
    public ResponseEntity<?> getResidentsList(@RequestParam Long currentUserId) {
        try {

            // send current user id to service layer to check role-based access
            List<Map<String, Object>> residents = userProfileService.getAllResidents(currentUserId);

            // if authorized & success, return residents list
            return ResponseEntity.ok(residents);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving residents: " + e.getMessage(), currentUserId);
        }
    }

    // get user profile data
    @GetMapping("/{userProfileId}/profile")
    public ResponseEntity<?> getProfileData(@PathVariable Long userProfileId, @RequestParam Long currentUserId) {
        try {

            // send userProfileId & currentUserId to service layer to check role-based access
            // we need to check role cus residents can only view their own profile, admins can view all, etc...
            Map<String, Object> profileResponse = userProfileService.getUserProfile(currentUserId, userProfileId);

            // if authorized & success, return user profile data
            return ResponseEntity.ok(profileResponse);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Error retrieving profile: " + e.getMessage(), currentUserId);
        }
    }

    // standard error response
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Long userId) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message, "userId", userId));
    }
}
