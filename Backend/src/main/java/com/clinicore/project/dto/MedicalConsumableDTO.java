package com.clinicore.project.dto;

import com.clinicore.project.entity.Item;
import com.clinicore.project.entity.MedicalConsumable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO for medical consumables - bandages, gloves, etc.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalConsumableDTO {

    private Long id;
    private String name;
    private Integer quantity;
    private Long supplierId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // convert entity to DTO
    public static MedicalConsumableDTO fromEntity(MedicalConsumable medicalConsumable) {
        if (medicalConsumable == null || medicalConsumable.getItem() == null) {
            return null;
        }

        return new MedicalConsumableDTO(
            medicalConsumable.getId(),
            medicalConsumable.getItem().getName(),
            medicalConsumable.getItem().getQuantity(),
            medicalConsumable.getItem().getSupplierId(),
            medicalConsumable.getItem().getCreated_at(),
            medicalConsumable.getItem().getUpdated_at()
        );
    }
    /**
     * Convert DTO to Entity
     */
    public MedicalConsumable toEntity() {
        MedicalConsumable consumable = new MedicalConsumable();

        if (consumable.getItem() == null) {
            consumable.setItem(new Item());
        }

        consumable.getItem().setName(this.name);
        consumable.getItem().setQuantity(this.quantity);

        return consumable;
    }

    /**
     * Update an existing entity
     */
    public void updateEntity(MedicalConsumable consumable) {
        if (consumable.getItem() == null) {
            consumable.setItem(new Item());
        }

        consumable.getItem().setName(this.name);
        consumable.getItem().setQuantity(this.quantity);
    }

}
