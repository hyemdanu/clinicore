package com.clinicore.project.service;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CaregiverService {

    private final CaregiverRepository caregiverRepository;
    private final UserProfileRepository userProfileRepository;
    private final ResidentCaregiverRepository residentCaregiverRepository;

    public CaregiverService(CaregiverRepository caregiverRepository,
                            UserProfileRepository userProfileRepository,
                            ResidentCaregiverRepository residentCaregiverRepository) {
        this.caregiverRepository = caregiverRepository;
        this.userProfileRepository = userProfileRepository;
        this.residentCaregiverRepository = residentCaregiverRepository;
    }

    // grab all caregivers + who they're assigned to, admins only
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllCaregivers(Long currentUserId) {
        UserProfile currentUser = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can view the caregiver list");
        }

        List<Caregiver> caregivers = caregiverRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Caregiver caregiver : caregivers) {
            UserProfile profile = caregiver.getUserProfile();

            Map<String, Object> caregiverMap = new LinkedHashMap<>();
            caregiverMap.put("id", profile.getId());
            caregiverMap.put("firstName", profile.getFirstName());
            caregiverMap.put("lastName", profile.getLastName());
            caregiverMap.put("email", profile.getEmail());
            caregiverMap.put("contactNumber", profile.getContactNumber());

            // look up which residents this caregiver is responsible for
            List<ResidentCaregiver> assignments = residentCaregiverRepository.findById_CaregiverId(profile.getId());
            List<Map<String, Object>> residentsList = new ArrayList<>();

            for (ResidentCaregiver assignment : assignments) {
                UserProfile residentProfile = assignment.getResident().getUserProfile();
                Map<String, Object> residentMap = new LinkedHashMap<>();
                residentMap.put("id", residentProfile.getId());
                residentMap.put("firstName", residentProfile.getFirstName());
                residentMap.put("lastName", residentProfile.getLastName());
                residentMap.put("assignedAt", assignment.getAssignedAt());
                residentsList.add(residentMap);
            }

            caregiverMap.put("residents", residentsList);
            result.add(caregiverMap);
        }

        return result;
    }
}
