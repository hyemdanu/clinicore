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

import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.entity.Resident;
import com.clinicore.project.entity.Caregiver;
import com.clinicore.project.entity.Admin;

import com.clinicore.project.repository.UserProfileRepository;
import com.clinicore.project.repository.ResidentGeneralRepository;
import com.clinicore.project.repository.CaregiverRepository;
import com.clinicore.project.repository.AdminRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserProfileController {

    // variables
    private final UserProfileRepository userProfileRepository;
    private final CaregiverRepository caregiverRepository;
    private final ResidentGeneralRepository residentRepository;
    private final AdminRepository adminRepository;

    // constructor that injects all repositories so the controller can access user and resident data
    public UserProfileController(UserProfileRepository userProfileRepository,
                                 CaregiverRepository caregiverRepository,
                                 ResidentGeneralRepository residentRepository,
                                 AdminRepository adminRepository) {
        this.userProfileRepository = userProfileRepository;
        this.caregiverRepository = caregiverRepository;
        this.residentRepository = residentRepository;
        this.adminRepository = adminRepository;
    }

    /**
     * Get a list of all residents (for caregiver and admin ONLY)
     * Backend should handle all authorization checks since if it's in frontend, people can edit that
     */
    @GetMapping("/residents/list")
    public ResponseEntity<?> getResidentsList(@RequestParam Long currentUserId) {

        // find current user
        UserProfile currentUser = userProfileRepository.findById(currentUserId).orElse(null);

        // error handling --> validate user exists
        if (currentUser == null) {
            return createErrorResponse(HttpStatus.NOT_FOUND, 
                    "Current user not found", currentUserId);
        }

        // authorization check since only CAREGIVER and ADMIN can view list of residents!
        if (currentUser.getRole() == UserProfile.Role.CAREGIVER || currentUser.getRole() == UserProfile.Role.ADMIN) {

            // fetch residents only from database (check role column)
            List<UserProfile> residents = userProfileRepository.findByRole(UserProfile.Role.RESIDENT);

            // create a list to store maps
            List<Map<String, Object>> residentList = new ArrayList<>();

            // loop through each resident
            for (UserProfile resident : residents) {

                // create a map for one resident
                Map<String, Object> residentInfo = new LinkedHashMap<>();
                residentInfo.put("id", resident.getId());
                residentInfo.put("firstName", resident.getFirstName());
                residentInfo.put("lastName", resident.getLastName());

                // add the map to the list of maps
                residentList.add(residentInfo);
            }

            // return the list of maps of residents
            return ResponseEntity.ok(residentList);

        } else {

            return createErrorResponse(HttpStatus.FORBIDDEN, "You do not have permission", currentUserId);

        }


    }

    /**
     * Get user profile with role-based authorization
     *
     * Role-based authorization:
     * - Residents can only view their own information
     * - Caregivers can view own information + list of residents + selected resident's information
     * - Admins can view own information + list of residents + selected resident's information
     *  will need to add list of caregivers later...
     *
     * @param userProfileId the ID of the user to retrieve
     * @param currentUserId the ID of the current user
     */
    @GetMapping("/{userProfileId}/profile")
    public ResponseEntity<?> getProfileData(@PathVariable Long userProfileId, @RequestParam Long currentUserId) {

        // validate current user exist
        UserProfile currentUser = userProfileRepository.findById(currentUserId).orElse(null);
        if (currentUser == null) {
            return createErrorResponse(HttpStatus.NOT_FOUND, 
                    "Current user not found", currentUserId);
        }

        // validate target user exist
        UserProfile targetUser = userProfileRepository.findById(userProfileId).orElse(null);
        if (targetUser == null) {
            return createErrorResponse(HttpStatus.NOT_FOUND, 
                    "Target user not found", userProfileId);
        }

        // check authorization access (what they can view)
        if (!isAuthorized(currentUserId, userProfileId, currentUser.getRole(), targetUser.getRole())) {
            return createErrorResponse(HttpStatus.FORBIDDEN,
                    "You do not have permission", currentUserId);
        }

        // if they do have access, build and return profile response
        // buildProfileResponse is a method down below (scroll down)
        // made a separate method for reusability... scaling purposes...
        Map<String, Object> profileResponse = buildProfileResponse(targetUser);
        return ResponseEntity.ok(profileResponse);
    }

    // ==================== Helper Methods ====================

    /**
     * Build profile response
     */
    private Map<String, Object> buildProfileResponse(UserProfile user) {
        Map<String, Object> response = new LinkedHashMap<>();
        
        // basic user information (all users have this)
        response.put("userProfileId", user.getId());
        response.put("username", user.getUsername());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("gender", user.getGender());
        response.put("birthday", user.getBirthday());
        response.put("contactNumber", user.getContactNumber());
        response.put("role", user.getRole().toString());

        // role-specific data
        switch (user.getRole()) {
            case CAREGIVER -> addCaregiverData(response, user.getId()); // this is also a method (scroll down)
            case RESIDENT -> addResidentData(response, user.getId()); // this is also a method (scroll down)
            case ADMIN -> {} // theres no additional data for admins so like non rlly to do

            // decided to do methods for separation + scaling purposes incase we need to add more data later
        }

        return response;
    }

    // to add caregiver specific data
    private void addCaregiverData(Map<String, Object> response, Long userId) {
        caregiverRepository.findById(userId).ifPresent(caregiver ->
                response.put("caregiverNotes", caregiver.getNotes())
        );
    }

    // to add resident specific data
    private void addResidentData(Map<String, Object> response, Long userId) {
        residentRepository.findById(userId).ifPresent(resident -> {
            response.put("emergencyContactName", resident.getEmergencyContactName());
            response.put("emergencyContactNumber", resident.getEmergencyContactNumber());
            response.put("residentNotes", resident.getNotes());
        });
    }

    // authorization check
    // can current user view target user's profile? let's find out headaa
    private boolean isAuthorized(Long currentUserId, Long targetUserId, // ids
                                 UserProfile.Role currentRole, UserProfile.Role targetRole) { // roles

        // allow viewing own profile
        if (currentUserId.equals(targetUserId)) {
            return true;
        }

        return switch (currentRole) {
            case RESIDENT -> false; // Residents can't view others
            case CAREGIVER -> targetRole == UserProfile.Role.RESIDENT; // Only residents
            case ADMIN -> true; // admins can view everyone
        };
    }

    /**
     * Create standardized error response
     */
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Long userId) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message, "userId", userId));
    }
}
