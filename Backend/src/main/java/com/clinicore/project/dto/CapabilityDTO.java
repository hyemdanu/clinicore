package com.clinicore.project.dto;

import com.clinicore.project.entity.Capability;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Capability entity
 * To avoid circular reference issues and over-fetching of data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapabilityDTO {

    private Long residentId;
    private Boolean verbal;
    private Boolean selfMedicates;
    private Capability.IncontinenceStatus incontinenceStatus;
    private Capability.MobilityStatus mobilityStatus;

    /**
     * Convert Capability entity to DTO
     */
    public static CapabilityDTO fromEntity(Capability capability) {
        if (capability == null) {
            return null;
        }

        return new CapabilityDTO(
            capability.getResidentId(),
            capability.getVerbal(),
            capability.getSelfMedicates(),
            capability.getIncontinenceStatus(),
            capability.getMobilityStatus()
        );
    }
}
