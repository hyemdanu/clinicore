package com.clinicore.project.service;

import com.clinicore.project.dto.MedicationDTO;
import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResidentMedicationInformationService {

    private final MedicationRepository medicationRepository;
    private final UserProfileRepository userProfileRepository;

    public ResidentMedicationInformationService(MedicationRepository medicationRepository,
                                               UserProfileRepository userProfileRepository) {
        this.medicationRepository = medicationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // Get all medications for a resident
    public List<MedicationDTO> getAllMedication(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        List<Medication> medications = medicationRepository.findByMedicalProfileResidentId(residentId);
        return medications.stream()
                .map(MedicationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Get medication name by medication ID
    public String getName(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        // Validate that the current user has permission to access this medication
        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getMedicationName();
    }

    // Get medication dosage
    public String getDosage(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getDosage();
    }

    // Get medication frequency
    public String getFrequency(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getFrequency();
    }

    // Get medication intake status
    public Medication.IntakeStatus getIntakeStatus(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getIntakeStatus();
    }

    // Get medication last administered time
    public LocalDateTime getLastAdministered(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getLastAdministeredAt();
    }

    // Get medication notes
    public String getNotes(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getNotes();
    }

    // Helper Methods

    private UserProfile getUserById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    private Medication getMedicationById(Long medicationId) {
        return medicationRepository.findById(medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with ID: " + medicationId));
    }

    private void validatePermissions(UserProfile currentUser, Long residentId) {
        // Admin and caregiver can access any resident's medication information
        if (currentUser.getRole() == UserProfile.Role.ADMIN ||
            currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            return;
        }

        // Residents can only access their own medication information
        if (currentUser.getRole() == UserProfile.Role.RESIDENT &&
            currentUser.getId().equals(residentId)) {
            return;
        }

        throw new IllegalArgumentException("You do not have permission to access this medication information");
    }
}
