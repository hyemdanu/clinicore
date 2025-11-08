package com.clinicore.project.service;

import com.clinicore.project.dto.CapabilityDTO;
import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResidentMedicalInformationService {

    private final MedicalProfileRepository medicalProfileRepository;
    private final UserProfileRepository userProfileRepository;

    public ResidentMedicalInformationService(MedicalProfileRepository medicalProfileRepository,
                                            UserProfileRepository userProfileRepository) {
        this.medicalProfileRepository = medicalProfileRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // Medical Profile Information Methods

    public String getInsurance(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        return medicalProfile.getInsurance();
    }

    public String getMedicalProfileNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        return medicalProfile.getNotes();
    }

    public CapabilityDTO getCapability(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        return CapabilityDTO.fromEntity(medicalProfile.getCapability());
    }

    // Medical Record Information Methods

    public List<Map<String, Object>> getAllAllergies(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();

        if (medicalRecord == null) {
            return Collections.emptyList();
        }

        List<String> allergies = medicalRecord.getAllergyList();
        return allergies.stream()
                .map(allergy -> Map.of("allergy", (Object) allergy))
                .collect(Collectors.toList());
    }

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

    public String getMedicalRecordNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();

        return medicalRecord != null ? medicalRecord.getNotes() : null;
    }

    // Medical Services Information Methods

    public String getHospiceAgency(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getHospiceAgency() : null;
    }

    public String getPreferredHospital(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getPreferredHospital() : null;
    }

    public String getPreferredPharmacy(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getPreferredPharmacy() : null;
    }

    public String getPreferredHomeHealthAgency(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getHomeHealthAgency() : null;
    }

    public String getMortuary(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getMortuary() : null;
    }

    public String getDNRPolst(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getDnrPolst() : null;
    }

    public Boolean getHospice(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getHospice() : null;
    }

    public Boolean getHomeHealth(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getHomeHealth() : null;
    }

    public String getMedicalServiceNotes(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        MedicalProfile medicalProfile = getMedicalProfileByResidentId(residentId);
        MedicalServices medicalServices = medicalProfile.getMedicalServices();

        return medicalServices != null ? medicalServices.getNotes() : null;
    }

    // Helper Methods

    private UserProfile getUserById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    private MedicalProfile getMedicalProfileByResidentId(Long residentId) {
        return medicalProfileRepository.findById(residentId)
                .orElseThrow(() -> new IllegalArgumentException("Medical profile not found for resident ID: " + residentId));
    }

    private void validatePermissions(UserProfile currentUser, Long residentId) {
        // Admin and caregiver can access any resident's medical information
        if (currentUser.getRole() == UserProfile.Role.ADMIN ||
            currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            return;
        }

        // Residents can only access their own medical information
        if (currentUser.getRole() == UserProfile.Role.RESIDENT &&
            currentUser.getId().equals(residentId)) {
            return;
        }

        throw new IllegalArgumentException("You do not have permission to access this medical information");
    }
}
