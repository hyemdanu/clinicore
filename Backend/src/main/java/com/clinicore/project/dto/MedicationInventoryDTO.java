package com.clinicore.project.dto;

import com.clinicore.project.entity.MedicationInventory;
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
    private String name;
    private Integer quantity;
    private String dosagePerServing;
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
}
