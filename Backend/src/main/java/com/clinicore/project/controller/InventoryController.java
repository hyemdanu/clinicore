package com.clinicore.project.controller;

import com.clinicore.project.dto.MedicalConsumableDTO;
import com.clinicore.project.dto.MedicationInventoryDTO;
import com.clinicore.project.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Inventory Controller
 */
@RestController
@RequestMapping("/api/inventory")
@CrossOrigin
public class InventoryController {

    // inject the service layer
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get all medication inventory items
     * Frontend uses this to display the full medication inventory table
     */
    @GetMapping("/medication")
    public ResponseEntity<?> getAllMedicationInventory(@RequestParam Long currentUserId) {
        try {
            List<MedicationInventoryDTO> medications = inventoryService.getAllMedicationInventory(currentUserId);

            return ResponseEntity.ok(medications);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving medication inventory: " + e.getMessage(), currentUserId);
        }
    }

    /**
     * Get medication inventory items with low stock
     * Filters medications where quantity <= threshold
     */
    @GetMapping("/medication/lowstock")
    public ResponseEntity<?> getLowStockMedications(@RequestParam Long currentUserId,
                                                     @RequestParam(required = false, defaultValue = "10") Integer threshold) {
        try {
            List<MedicationInventoryDTO> medications = inventoryService.getLowStockMedications(currentUserId, threshold);
            return ResponseEntity.ok(medications);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving low stock medications: " + e.getMessage(), currentUserId);
        }
    }

    /**
     * Get all medical consumables inventory items
     * Returns bandages, gloves, syringes, etc.
     */
    @GetMapping("/consumables")
    public ResponseEntity<?> getAllConsumablesInventory(@RequestParam Long currentUserId) {
        try {
            List<MedicalConsumableDTO> consumables = inventoryService.getAllConsumablesInventory(currentUserId);
            return ResponseEntity.ok(consumables);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving consumables inventory: " + e.getMessage(), currentUserId);
        }
    }

    /**
     * Get medical consumables inventory items with low stock
     * Same as medication lowstock but for consumables
     */
    @GetMapping("/consumables/lowstock")
    public ResponseEntity<?> getLowStockConsumables(@RequestParam Long currentUserId,
                                                     @RequestParam(required = false, defaultValue = "10") Integer threshold) {
        try {
            List<MedicalConsumableDTO> consumables = inventoryService.getLowStockConsumables(currentUserId, threshold);
            return ResponseEntity.ok(consumables);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving low stock consumables: " + e.getMessage(), currentUserId);
        }
    }

    /**
     * Get a specific medication inventory item by ID
     */
    @GetMapping("/medication/{itemId}")
    public ResponseEntity<?> getMedicationInventoryById(@RequestParam Long currentUserId,
                                                         @PathVariable Long itemId) {
        try {
            MedicationInventoryDTO medication = inventoryService.getMedicationInventoryById(currentUserId, itemId);
            return ResponseEntity.ok(medication);

        } catch (IllegalArgumentException e) {
            // could be permission issue OR item not found
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving medication inventory item: " + e.getMessage(), currentUserId);
        }
    }

    /**
     * Get a specific medical consumable inventory item by ID
     */
    @GetMapping("/consumables/{itemId}")
    public ResponseEntity<?> getConsumableInventoryById(@RequestParam Long currentUserId,
                                                         @PathVariable Long itemId) {
        try {
            MedicalConsumableDTO consumable = inventoryService.getConsumableInventoryById(currentUserId, itemId);
            return ResponseEntity.ok(consumable);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.FORBIDDEN, e.getMessage(), currentUserId);

        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error retrieving consumable inventory item: " + e.getMessage(), currentUserId);
        }
    }
    /**
     * Create a standardized error response
     */
    private ResponseEntity<?> createErrorResponse(HttpStatus status, String message, Long userId) {
        return ResponseEntity.status(status)
                .body(Map.of("message", message, "userId", userId));
    }
}
