package com.clinicore.project.dto;

import com.clinicore.project.entity.Item;
import com.clinicore.project.entity.MedicationInventory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO for medication inventory - stock of meds we have on hand
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationInventoryDTO {

    private Long id;

    @NotBlank(message = "Medication name is required")
    @Size(max = 255, message = "Medication name must be under 255 characters")
    private String name;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Size(max = 255, message = "Dosage must be under 255 characters")
    private String dosagePerServing;

    @Size(max = 1000, message = "Notes must be under 1000 characters")
    private String notes;

    private Long supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // convert entity to DTO


    public static MedicationInventoryDTO fromEntity(MedicationInventory medicationInventory) {
        if (medicationInventory == null || medicationInventory.getItem() == null) {
            return null;
        }

        return new MedicationInventoryDTO(
            medicationInventory.getId(),
            medicationInventory.getItem().getName(),
            medicationInventory.getItem().getQuantity(),
            medicationInventory.getDosagePerServing(),
            medicationInventory.getNotes(),
            medicationInventory.getItem().getSupplierId(),
            medicationInventory.getItem().getCreated_at(),
            medicationInventory.getItem().getUpdated_at()
        );
    }

    /**
     * Convert DTO to Entity
     */
    public MedicationInventory toEntity() {
        MedicationInventory medication = new MedicationInventory();

        if (medication.getItem() == null) {
            medication.setItem(new Item());
        }

        medication.getItem().setName(this.name);
        medication.getItem().setQuantity(this.quantity);

        return medication;
    }

    /**
     * Update an existing entity
     */
    public void updateEntity(MedicationInventory medication) {
        if (medication.getItem() == null) {
            medication.setItem(new Item());
        }

        medication.getItem().setName(this.name);
        medication.getItem().setQuantity(this.quantity);
    }
}
