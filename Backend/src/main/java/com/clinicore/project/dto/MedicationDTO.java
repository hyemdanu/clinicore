package com.clinicore.project.dto;

import com.clinicore.project.entity.Medication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Medication entity
 * To avoid circular reference issues and over-fetching of data - Edison
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDTO {

    private Long id;
    private String medicationName;
    private String dosage;
    private String frequency;
    private Medication.IntakeStatus intakeStatus;
    private LocalDateTime lastAdministeredAt;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Medication entity to DTO
     */
    public static MedicationDTO fromEntity(Medication medication) {
        if (medication == null) {
            return null;
        }

        return new MedicationDTO(
            medication.getId(),
            medication.getMedicationName(),
            medication.getDosage(),
            medication.getFrequency(),
            medication.getIntakeStatus(),
            medication.getLastAdministeredAt(),
            medication.getNotes(),
            medication.getCreatedAt(),
            medication.getUpdatedAt()
        );
    }
}
