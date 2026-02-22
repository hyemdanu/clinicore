package com.clinicore.project.service;

import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CaregiverService {

    private final CaregiverRepository caregiverRepository;
    private final UserProfileRepository userProfileRepository;
    private final ResidentCaregiverRepository residentCaregiverRepository;
    private final ResidentGeneralRepository residentGeneralRepository;

    public CaregiverService(CaregiverRepository caregiverRepository,
                            UserProfileRepository userProfileRepository,
                            ResidentCaregiverRepository residentCaregiverRepository,
                            ResidentGeneralRepository residentGeneralRepository) {
        this.caregiverRepository = caregiverRepository;
        this.userProfileRepository = userProfileRepository;
        this.residentCaregiverRepository = residentCaregiverRepository;
        this.residentGeneralRepository = residentGeneralRepository;
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

    // get all residents (id, firstName, lastName) for dropdowns — admins only
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllResidents(Long currentUserId) {
        UserProfile currentUser = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can access this");
        }

        List<UserProfile> residents = userProfileRepository.findByRole(UserProfile.Role.RESIDENT);
        List<Map<String, Object>> result = new ArrayList<>();

        for (UserProfile r : residents) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("firstName", r.getFirstName());
            m.put("lastName", r.getLastName());
            result.add(m);
        }

        return result;
    }

    // assign a resident to a caregiver
    @Transactional
    public void assignResident(Long caregiverId, Long residentId, Long currentUserId) {
        UserProfile currentUser = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can manage assignments");
        }

        Caregiver caregiver = caregiverRepository.findById(caregiverId)
                .orElseThrow(() -> new IllegalArgumentException("Caregiver not found with ID: " + caregiverId));

        Resident resident = residentGeneralRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found with ID: " + residentId));

        ResidentCaregiverId id = new ResidentCaregiverId(residentId, caregiverId);
        if (residentCaregiverRepository.existsById(id)) {
            throw new IllegalArgumentException("Resident is already assigned to this caregiver");
        }

        ResidentCaregiver assignment = new ResidentCaregiver(id, resident, caregiver, LocalDateTime.now());
        residentCaregiverRepository.save(assignment);
    }

    // remove a resident from a caregiver
    @Transactional
    public void removeResident(Long caregiverId, Long residentId, Long currentUserId) {
        UserProfile currentUser = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can manage assignments");
        }

        ResidentCaregiverId id = new ResidentCaregiverId(residentId, caregiverId);
        if (!residentCaregiverRepository.existsById(id)) {
            throw new IllegalArgumentException("Assignment not found");
        }

        residentCaregiverRepository.deleteById(id);
    }

    // switch a resident from one caregiver to another
    @Transactional
    public void switchResident(Long fromCaregiverId, Long residentId, Long toCaregiverId, Long currentUserId) {
        UserProfile currentUser = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (currentUser.getRole() != UserProfile.Role.ADMIN) {
            throw new IllegalArgumentException("Only admins can manage assignments");
        }

        if (fromCaregiverId.equals(toCaregiverId)) {
            throw new IllegalArgumentException("Cannot switch to the same caregiver");
        }

        Caregiver toCaregiver = caregiverRepository.findById(toCaregiverId)
                .orElseThrow(() -> new IllegalArgumentException("Target caregiver not found with ID: " + toCaregiverId));

        Resident resident = residentGeneralRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found with ID: " + residentId));

        // remove from old caregiver
        ResidentCaregiverId oldId = new ResidentCaregiverId(residentId, fromCaregiverId);
        if (!residentCaregiverRepository.existsById(oldId)) {
            throw new IllegalArgumentException("Assignment not found");
        }
        residentCaregiverRepository.deleteById(oldId);

        // assign to new caregiver (skip if already assigned)
        ResidentCaregiverId newId = new ResidentCaregiverId(residentId, toCaregiverId);
        if (!residentCaregiverRepository.existsById(newId)) {
            ResidentCaregiver newAssignment = new ResidentCaregiver(newId, resident, toCaregiver, LocalDateTime.now());
            residentCaregiverRepository.save(newAssignment);
        }
    }

    // returns residents split into assigned/others for a caregiver's resident tab
    @Transactional(readOnly = true)
    public Map<String, Object> getResidentsSplitForCaregiver(Long currentUserId) {
        UserProfile currentUser = userProfileRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + currentUserId));

        if (currentUser.getRole() != UserProfile.Role.CAREGIVER) {
            throw new IllegalArgumentException("Only caregivers can access this endpoint");
        }

        // get all residents assigned to this caregiver
        List<ResidentCaregiver> myAssignments = residentCaregiverRepository.findById_CaregiverId(currentUserId);
        Set<Long> myResidentIds = myAssignments.stream()
                .map(a -> a.getResident().getUserProfile().getId())
                .collect(Collectors.toSet());

        // get all residents
        List<UserProfile> allResidents = userProfileRepository.findByRole(UserProfile.Role.RESIDENT);

        List<Map<String, Object>> assigned = new ArrayList<>();
        List<Map<String, Object>> others = new ArrayList<>();

        for (UserProfile resident : allResidents) {
            // get all caregivers assigned to this resident
            List<ResidentCaregiver> residentAssignments = residentCaregiverRepository.findById_ResidentId(resident.getId());

            Map<String, Object> residentMap = new LinkedHashMap<>();
            residentMap.put("id", resident.getId());
            residentMap.put("firstName", resident.getFirstName());
            residentMap.put("lastName", resident.getLastName());

            if (myResidentIds.contains(resident.getId())) {
                assigned.add(residentMap);
            } else {
                // add assigned caregiver names for the indicator
                List<String> caregiverNames = residentAssignments.stream()
                        .map(a -> a.getCaregiver().getUserProfile().getFirstName() + " " + a.getCaregiver().getUserProfile().getLastName())
                        .collect(Collectors.toList());
                residentMap.put("assignedCaregivers", caregiverNames);
                others.add(residentMap);
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assigned", assigned);
        result.put("others", others);
        return result;
    }
}
