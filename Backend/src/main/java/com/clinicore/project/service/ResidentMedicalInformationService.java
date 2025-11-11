package com.clinicore.project.service;

import com.clinicore.project.dto.CapabilityDTO;
import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resident Medical Information Service
 * handles all resident medical data like insurance, allergies, diagnoses, etc
 */
@Service
public class ResidentMedicalInformationService {

    // repositories for db access
    private final MedicalProfileRepository medicalProfileRepository;
    private final UserProfileRepository userProfileRepository;

    public ResidentMedicalInformationService(MedicalProfileRepository medicalProfileRepository,
                                            UserProfileRepository userProfileRepository) {
        this.medicalProfileRepository = medicalProfileRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // get resident insurance info
    public String getInsurance(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        return medicalProfile.getInsurance();
    }

    // get notes from medical profile
    public String getMedicalProfileNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        return medicalProfile.getNotes();
    }

    // get capability info (mobility, cognitive status, etc)
    public CapabilityDTO getCapability(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        return CapabilityDTO.fromEntity(medicalProfile.getCapability());
    }

    // get all allergies for resident
    public List<Map<String, Object>> getAllAllergies(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();

        if (medicalRecord == null) {
            return Collections.emptyList();
        }

        // convert list of allergies to list of maps for json response
        List<String> allergies = medicalRecord.getAllergyList();
        return allergies.stream()
                .map(allergy -> Map.of("allergy", (Object) allergy))
                .collect(Collectors.toList());
    }

    // get all diagnoses for resident
    public List<Map<String, Object>> getAllDiagnoses(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();

        if (medicalRecord == null) {
            return Collections.emptyList();
        }

        List<String> diagnoses = medicalRecord.getDiagnosisList();
        return diagnoses.stream()
                .map(diagnosis -> Map.of("diagnosis", (Object) diagnosis))
                .collect(Collectors.toList());
    }

    // get medical record notes
    public String getMedicalRecordNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();

        return medicalRecord != null ? medicalRecord.getNotes() : null;
    }

    // get hospice agency name
    public String getHospiceAgency(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getHospiceAgency() : null;
    }

    // get preferred hospital
    public String getPreferredHospital(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getPreferredHospital() : null;
    }

    // get preferred pharmacy
    public String getPreferredPharmacy(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getPreferredPharmacy() : null;
    }

    // get home health agency
    public String getPreferredHomeHealthAgency(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getHomeHealthAgency() : null;
    }

    // get mortuary info
    public String getMortuary(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getMortuary() : null;
    }

    // get DNR/POLST status
    public String getDNRPolst(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getDnrPolst() : null;
    }

    // check if resident is on hospice
    public Boolean getHospice(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getHospice() : null;
    }

    // check if resident gets home health services
    public Boolean getHomeHealth(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getHomeHealth() : null;
    }

    // get medical services notes
    public String getMedicalServiceNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getNotes() : null;
    }

    // find user by ID
    private UserProfile getUserById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    // find medical profile by resident ID
    private MedicalProfile getMedicalProfileByResidentId(Long residentId) {
        return medicalProfileRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Medical profile not found for resident ID: " + residentId));
    }

    // check permissions
    private void validatePermissions(UserProfile currentUser, Long residentId) {
        // admin and caregiver can access any resident's medical info
        if (currentUser.getRole() == UserProfile.Role.ADMIN ||
            currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            return;
        }

        // residents can only see their own stuff
        if (currentUser.getRole() == UserProfile.Role.RESIDENT &&
            currentUser.getId().equals(residentId)) {
            return;
        }

        throw new IllegalArgumentException("You do not have permission to access this medical information");
    }
}
