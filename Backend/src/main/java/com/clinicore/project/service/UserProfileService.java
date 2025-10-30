/**
 * Olei Amelie Ngan & Edison Ho
 *
 * Service layer for user profiles (Caregiver, Resident, Admin)
 * This service layer uses the user profile repository to retrieve user information
 *
 * Functions/Purposes:
 * - Get user profile with authorization
 *
 */
package com.clinicore.project.service;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserProfileService {

    // variables
    private final UserProfileRepository userProfileRepository;
    private final CaregiverRepository caregiverRepository;
    private final ResidentGeneralRepository residentRepository;
    private final AdminRepository adminRepository;

    // construct the service layer with all repositories
    public UserProfileService(UserProfileRepository userProfileRepository,
                              CaregiverRepository caregiverRepository,
                              ResidentGeneralRepository residentRepository,
                              AdminRepository adminRepository) {
        this.userProfileRepository = userProfileRepository;
        this.caregiverRepository = caregiverRepository;
        this.residentRepository = residentRepository;
        this.adminRepository = adminRepository;
    }

    // get list of residents for caregivers/admins only
    public List<Map<String, Object>> getAllResidents(Long currentUserId) {
        UserProfile currentUser = getUserById(currentUserId);

        // check if caregiver or admin
        if (currentUser.getRole() != UserProfile.Role.CAREGIVER && currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("You do not have permission to view residents"); // if not, gtfo
        }

        // grab all residents
        List<UserProfile> residents = userProfileRepository.findByRole(UserProfile.Role.RESIDENT);

        // list to hold resident information map (a list of lists)
        // example: {{"id": 1, "firstName": "John", "lastName": "Doe"}, {"id": 2, "firstName": "Jane", "lastName": "Doe"}}
        List<Map<String, Object>> residentList = new ArrayList<>();

        // for each resident, add to list
        for (UserProfile resident : residents) {

            // build resident information
            Map<String, Object> residentInfo = new LinkedHashMap<>();
            residentInfo.put("id", resident.getId());
            residentInfo.put("firstName", resident.getFirstName());
            residentInfo.put("lastName", resident.getLastName());

            // add resident information to list
            residentList.add(residentInfo);
        }

        return residentList; // JSON FORMAT

    }

    // role-based access to user information
    // residents can only view their own profile, caregivers can view residents, admins can view everyone, etc...
    public Map<String, Object> getUserProfile(Long currentUserId, Long targetUserId) {

        // check if current user and target user exist via getUserbyId
        // if it does, store into currentUser and targetUser variables
        UserProfile currentUser = getUserById(currentUserId);
        UserProfile targetUser = getUserById(targetUserId);

        // check if current user is authorized to view target user
        if (!isAuthorized(currentUserId, targetUserId, currentUser.getRole(), targetUser.getRole())) {
            throw new IllegalArgumentException("You do not have permission to view this profile");
        }

        // map to store response
        Map<String, Object> response = new LinkedHashMap<>();

        // basic target user information (information to return)
        response.put("userProfileId", targetUser.getId());
        response.put("username", targetUser.getUsername());
        response.put("firstName", targetUser.getFirstName());
        response.put("lastName", targetUser.getLastName());
        response.put("gender", targetUser.getGender());
        response.put("birthday", targetUser.getBirthday());
        response.put("contactNumber", targetUser.getContactNumber());
        response.put("role", targetUser.getRole().toString());

        // add role specific data
        switch (targetUser.getRole()) {
            case CAREGIVER -> addCaregiverData(response, targetUser.getId()); // add caregiver data
            case RESIDENT -> addResidentData(response, targetUser.getId()); // add resident data
            case ADMIN -> {}  // no additional data for admins, so nothin todo here...
        }

        return response;

    }

    // validate user existence by ID (own method for reusability and scaling...)
    public UserProfile getUserById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    // to check if current user is authorized to view target user
    // it grabs current user id and role + target user id and role
    private boolean isAuthorized(Long currentUserId, Long targetUserId, UserProfile.Role currentRole, UserProfile.Role targetRole) {

        // allow own profile view
        if (currentUserId.equals(targetUserId)) {
            return true;
        }

        // role-based access
        return switch (currentRole) {
            case RESIDENT -> false;  // Residents can't view others
            case CAREGIVER -> targetRole == UserProfile.Role.RESIDENT;  // Caregivers can only view residents
            case ADMIN -> true;  // Admins can view everyone
        };
    }

    // add caregiver data to response
    private void addCaregiverData(Map<String, Object> response, Long userId) {
        caregiverRepository.findById(userId).ifPresent(caregiver ->
                response.put("caregiverNotes", caregiver.getNotes())
        );
    }

    // add resident data to response
    private void addResidentData(Map<String, Object> response, Long userId) {
        residentRepository.findById(userId).ifPresent(resident -> {
            response.put("emergencyContactName", resident.getEmergencyContactName());
            response.put("emergencyContactNumber", resident.getEmergencyContactNumber());
            response.put("residentNotes", resident.getNotes());
        });
    }


}