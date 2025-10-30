
package com.clinicore.project.service;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final CaregiverRepository caregiverRepository;
    private final ResidentGeneralRepository residentRepository;
    private final AdminRepository adminRepository;

    // Constructor injection
    public UserProfileService(UserProfileRepository userProfileRepository,
                              CaregiverRepository caregiverRepository,
                              ResidentGeneralRepository residentRepository,
                              AdminRepository adminRepository) {
        this.userProfileRepository = userProfileRepository;
        this.caregiverRepository = caregiverRepository;
        this.residentRepository = residentRepository;
        this.adminRepository = adminRepository;
    }

    /**
     * Get all residents (only for authorized users)
     */
    public List<Map<String, Object>> getAllResidents(Long currentUserId) {
        UserProfile currentUser = getUserById(currentUserId);

        // Authorization check
        if (currentUser.getRole() != UserProfile.Role.CAREGIVER && currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("You do not have permission to view residents");
        }

        // Fetch all residents
        List<UserProfile> residents = userProfileRepository.findByRole(UserProfile.Role.RESIDENT);

        // Transform to response format
        return transformResidentsToResponse(residents);
    }

    /**
     * Get user profile with authorization
     */
    public Map<String, Object> getUserProfile(Long currentUserId, Long targetUserId) {
        UserProfile currentUser = getUserById(currentUserId);
        UserProfile targetUser = getUserById(targetUserId);

        // Authorization check
        if (!isAuthorized(currentUserId, targetUserId, currentUser.getRole(), targetUser.getRole())) {
            throw new IllegalArgumentException("You do not have permission to view this profile");
        }

        // Build and return profile response
        return buildProfileResponse(targetUser);
    }

    /**
     * Get user by ID
     */
    public UserProfile getUserById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    /**
     * Check if current user is authorized to view target user
     */
    private boolean isAuthorized(Long currentUserId, Long targetUserId,
                                 UserProfile.Role currentRole, UserProfile.Role targetRole) {
        // Allow viewing own profile
        if (currentUserId.equals(targetUserId)) {
            return true;
        }

        return switch (currentRole) {
            case RESIDENT -> false;  // Residents can't view others
            case CAREGIVER -> targetRole == UserProfile.Role.RESIDENT;  // Caregivers can only view residents
            case ADMIN -> true;  // Admins can view everyone
        };
    }

    /**
     * Build profile response with role-specific data
     */
    private Map<String, Object> buildProfileResponse(UserProfile user) {
        Map<String, Object> response = new LinkedHashMap<>();

        // Basic user information
        response.put("userProfileId", user.getId());
        response.put("username", user.getUsername());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("gender", user.getGender());
        response.put("birthday", user.getBirthday());
        response.put("contactNumber", user.getContactNumber());
        response.put("role", user.getRole().toString());

        // Add role-specific data
        switch (user.getRole()) {
            case CAREGIVER -> addCaregiverData(response, user.getId());
            case RESIDENT -> addResidentData(response, user.getId());
            case ADMIN -> {}  // No additional data for admins
        }

        return response;
    }

    /**
     * Add caregiver-specific data to response
     */
    private void addCaregiverData(Map<String, Object> response, Long userId) {
        caregiverRepository.findById(userId).ifPresent(caregiver ->
                response.put("caregiverNotes", caregiver.getNotes())
        );
    }

    /**
     * Add resident-specific data to response
     */
    private void addResidentData(Map<String, Object> response, Long userId) {
        residentRepository.findById(userId).ifPresent(resident -> {
            response.put("emergencyContactName", resident.getEmergencyContactName());
            response.put("emergencyContactNumber", resident.getEmergencyContactNumber());
            response.put("residentNotes", resident.getNotes());
        });
    }

    /**
     * Transform residents to response format
     */
    private List<Map<String, Object>> transformResidentsToResponse(List<UserProfile> residents) {
        List<Map<String, Object>> residentList = new ArrayList<>();

        for (UserProfile resident : residents) {
            Map<String, Object> residentInfo = new LinkedHashMap<>();
            residentInfo.put("id", resident.getId());
            residentInfo.put("firstName", resident.getFirstName());
            residentInfo.put("lastName", resident.getLastName());
            residentList.add(residentInfo);
        }

        return residentList;
    }
}