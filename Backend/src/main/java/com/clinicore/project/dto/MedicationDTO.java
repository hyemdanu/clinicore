package com.clinicore.project.dto;

import com.clinicore.project.entity.Medication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// DTO for medication records - what meds a resident takes
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

    // convert entity to DTO
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
