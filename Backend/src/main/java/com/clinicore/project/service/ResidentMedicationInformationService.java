package com.clinicore.project.service;

import com.clinicore.project.dto.MedicationDTO;
import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Resident Medication Information Service
 * handles all resident medication data like dosage, frequency, intake status, etc
 */
@Service
public class ResidentMedicationInformationService {

    // repositories for db access
    private final MedicationRepository medicationRepository;
    private final UserProfileRepository userProfileRepository;

    public ResidentMedicationInformationService(MedicationRepository medicationRepository,
                                               UserProfileRepository userProfileRepository) {
        this.medicationRepository = medicationRepository;
        this.userProfileRepository = userProfileRepository;
    }

    // get all medications for a resident
    public List<MedicationDTO> getAllMedication(Long currentUserId, Long residentId) {
        UserProfile currentUser = getUserById(currentUserId);
        validatePermissions(currentUser, residentId);

        List<Medication> medications = medicationRepository.findByMedicalProfileResidentId(residentId);
        return medications.stream()
                .map(MedicationDTO::fromEntity)
                .collect(Collectors.toList());
    }


    // get medication name by ID
    public String getName(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getMedicationName();
    }

    // get medication dosage
    public String getDosage(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getDosage();
    }

    // get medication frequency
    public String getFrequency(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getFrequency();
    }

    // get medication intake status
    public Medication.IntakeStatus getIntakeStatus(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getIntakeStatus();
    }

    // get when medication was last administered
    public LocalDateTime getLastAdministered(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getLastAdministeredAt();
    }

    // get medication notes
    public String getNotes(Long currentUserId, Long medicationId) {
        UserProfile currentUser = getUserById(currentUserId);
        Medication medication = getMedicationById(medicationId);

        Long residentId = medication.getMedicalProfile().getResidentId();
        validatePermissions(currentUser, residentId);

        return medication.getNotes();
    }

    // find user by ID
    private UserProfile getUserById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    // find medication by ID
    private Medication getMedicationById(Long medicationId) {
        return medicationRepository.findById(medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with ID: " + medicationId));
    }

    // check permissions - admin/caregiver can see all, residents only see their own
    private void validatePermissions(UserProfile currentUser, Long residentId) {
        // admin and caregiver can access any resident's medication info
        if (currentUser.getRole() == UserProfile.Role.ADMIN ||
            currentUser.getRole() == UserProfile.Role.CAREGIVER) {
            return;
        }

        // residents can only see their own stuff
        if (currentUser.getRole() == UserProfile.Role.RESIDENT &&
            currentUser.getId().equals(residentId)) {
            return;
        }

        throw new IllegalArgumentException("You do not have permission to access this medication information");
    }
}
