package com.clinicore.project.service;

import com.clinicore.project.dto.MedicalConsumableDTO;
import com.clinicore.project.dto.MedicationInventoryDTO;
import com.clinicore.project.entity.MedicalConsumable;
import com.clinicore.project.entity.MedicationInventory;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.MedicalConsumableRepository;
import com.clinicore.project.repository.MedicationInventoryRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Service - handles all business logic for inventory management
 * Used to fetch medication and consumable inventory data
 * Only admin or caregiver roles have access to these methods
 */
@Service
public class InventoryService {

    // repositories for accessing the database

    private final MedicationInventoryRepository medicationInventoryRepository;
    private final MedicalConsumableRepository medicalConsumableRepository;
    private final UserProfileRepository userProfileRepository;

    // constructor injection of repositories
    public InventoryService(MedicationInventoryRepository medicationInventoryRepository,
                           MedicalConsumableRepository medicalConsumableRepository,
                           UserProfileRepository userProfileRepository) {
        this.medicationInventoryRepository = medicationInventoryRepository;
        this.medicalConsumableRepository = medicalConsumableRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * Get all medication inventory items
     * gets full list of medications in inventory
     */
    public List<MedicationInventoryDTO> getAllMedicationInventory(Long currentUserId) {
        // check user permissions
        validateAdminOrCaregiver(currentUserId);

        // fetch all medications from database, ordered by name
        List<MedicationInventory> medications = medicationInventoryRepository.findAllOrderedByName();

        // convert entities to DTOs to avoid circular references and over-fetching
        return medications.stream()
                .map(MedicationInventoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /*
     * Get medication inventory items with low stock
    */


    public List<MedicationInventoryDTO> getLowStockMedications(Long currentUserId, Integer threshold) {
        validateAdminOrCaregiver(currentUserId);

        // theshold for low stock
        if (threshold == null || threshold < 0) {
            threshold = 10;
        }

        final Integer finalThreshold = threshold;
        List<MedicationInventory> medications = medicationInventoryRepository.findAllOrderedByName();

        return medications.stream()
                .filter(med -> med.getItem() != null && med.getItem().getQuantity() <= finalThreshold)
                .map(MedicationInventoryDTO::fromEntity)
                .collect(Collectors.toList());
    }


    /**
     * Get all medical consumables inventory items
     * returns all consumables in inventory
     */
    public List<MedicalConsumableDTO> getAllConsumablesInventory(Long currentUserId) {
        validateAdminOrCaregiver(currentUserId);

        List<MedicalConsumable> consumables = medicalConsumableRepository.findAllOrderedByName();

        return consumables.stream()
                .map(MedicalConsumableDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get medical consumables inventory items with low stock
     * Same logic as medications but for consumables
     */
    public List<MedicalConsumableDTO> getLowStockConsumables(Long currentUserId, Integer threshold) {
        validateAdminOrCaregiver(currentUserId);

        // default threshold is 10
        if (threshold == null || threshold < 0) {
            threshold = 10;
        }

        // make final for lambda usage
        final Integer finalThreshold = threshold;
        List<MedicalConsumable> consumables = medicalConsumableRepository.findAllOrderedByName();

        // filter consumables at or below threshold
        return consumables.stream()
                .filter(consumable -> consumable.getItem() != null && consumable.getItem().getQuantity() <= finalThreshold)
                .map(MedicalConsumableDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific medication inventory item by ID
     * Useful for viewing details or updating a single item
     */
    public MedicationInventoryDTO getMedicationInventoryById(Long currentUserId, Long itemId) {
        validateAdminOrCaregiver(currentUserId);

        // find by ID or throw exception if not found
        // this is cleaner than returning null
        MedicationInventory medication = medicationInventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Medication inventory item not found with ID: " + itemId));

        return MedicationInventoryDTO.fromEntity(medication);
    }

    /**
     * Get a specific medical consumable inventory item by ID
     */
    public MedicalConsumableDTO getConsumableInventoryById(Long currentUserId, Long itemId) {
        validateAdminOrCaregiver(currentUserId);

        // find or throw exception
        MedicalConsumable consumable = medicalConsumableRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Medical consumable inventory item not found with ID: " + itemId));

        return MedicalConsumableDTO.fromEntity(consumable);
    }

    /**
     * Create a new medication inventory item
     * NEW METHOD
     */
    public MedicationInventoryDTO createMedicationInventory(Long currentUserId, MedicationInventoryDTO medicationDTO) {
        validateAdminOrCaregiver(currentUserId);

        UserProfile currentUser = getUserById(currentUserId);

        // Create new medication entity from DTO
        MedicationInventory medication = medicationDTO.toEntity();
        medication.setUserProfile(currentUser);

        // Save to database
        MedicationInventory saved = medicationInventoryRepository.save(medication);

        return MedicationInventoryDTO.fromEntity(saved);
    }

    /**
     * Update an existing medication inventory item
     * NEW METHOD
     */
    public MedicationInventoryDTO updateMedicationInventory(Long currentUserId, Long itemId, MedicationInventoryDTO medicationDTO) {
        validateAdminOrCaregiver(currentUserId);

        // Find existing medication
        MedicationInventory medication = medicationInventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Medication inventory item not found with ID: " + itemId));

        // Update the medication with new data from DTO
        medicationDTO.updateEntity(medication);

        // Save updated medication
        MedicationInventory updated = medicationInventoryRepository.save(medication);

        return MedicationInventoryDTO.fromEntity(updated);
    }

    /**
     * Delete a medication inventory item
     * NEW METHOD
     */
    public void deleteMedicationInventory(Long currentUserId, Long itemId) {
        validateAdminOrCaregiver(currentUserId);

        // Find medication to delete
        MedicationInventory medication = medicationInventoryRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Medication inventory item not found with ID: " + itemId));

        // Delete from database
        medicationInventoryRepository.delete(medication);
    }

    /**
     * Create a new consumable inventory item
     * NEW METHOD
     */
    public MedicalConsumableDTO createConsumableInventory(Long currentUserId, MedicalConsumableDTO consumableDTO) {
        validateAdminOrCaregiver(currentUserId);

        UserProfile currentUser = getUserById(currentUserId);

        // Create new consumable entity from DTO
        MedicalConsumable consumable = consumableDTO.toEntity();
        consumable.setUserProfile(currentUser);

        // Save to database
        MedicalConsumable saved = medicalConsumableRepository.save(consumable);

        return MedicalConsumableDTO.fromEntity(saved);
    }

    /**
     * Update an existing consumable inventory item
     * NEW METHOD
     */
    public MedicalConsumableDTO updateConsumableInventory(Long currentUserId, Long itemId, MedicalConsumableDTO consumableDTO) {
        validateAdminOrCaregiver(currentUserId);

        // Find existing consumable
        MedicalConsumable consumable = medicalConsumableRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Medical consumable inventory item not found with ID: " + itemId));

        // Update the consumable with new data from DTO
        consumableDTO.updateEntity(consumable);

        // Save updated consumable
        MedicalConsumable updated = medicalConsumableRepository.save(consumable);

        return MedicalConsumableDTO.fromEntity(updated);
    }

    /**
     * Delete a consumable inventory item
     * NEW METHOD
     */
    public void deleteConsumableInventory(Long currentUserId, Long itemId) {
        validateAdminOrCaregiver(currentUserId);

        // Find consumable to delete
        MedicalConsumable consumable = medicalConsumableRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Medical consumable inventory item not found with ID: " + itemId));

        // Delete from database
        medicalConsumableRepository.delete(consumable);
    }


    /**
     * Find user by ID
     */
    private UserProfile getUserById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    /**
     * Validate that current user is either admin or caregiver
     * Residents should not have access to inventory management
     */
    private void validateAdminOrCaregiver(Long currentUserId) {
        UserProfile currentUser = getUserById(currentUserId);

        if (currentUser.getRole() != UserProfile.Role.ADMIN &&
            currentUser.getRole() != UserProfile.Role.CAREGIVER) {
            throw new IllegalArgumentException("You do not have permission to access inventory information");
        }
    }
}
