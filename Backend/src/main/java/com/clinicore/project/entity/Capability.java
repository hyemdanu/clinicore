package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "capability")
public class Capability {

    @Id
    private Long resident_id;

    private Boolean verbal;
    private Boolean self_medicates;
    
    @Enumerated(EnumType.STRING)
    private IncontinenceStatus incontinence_status;
    
    @Enumerated(EnumType.STRING)
    private MobilityStatus mobility_status;

    // Enums
    public enum IncontinenceStatus {
        CONTINENT,
        INCONTINENT_URINE,
        INCONTINENT_BOWELS,
        INCONTINENT_BOTH
    }

    public enum MobilityStatus {
        WALKS_WITHOUT_ASSISTANCE,
        WALKS_WITH_ASSISTANCE,
        WHEELCHAIR,
        BEDRIDDEN
    }

    // Owned side - relationship to MedicalProfile
    @OneToOne
    @MapsId
    @JoinColumn(name = "resident_id")
    private MedicalProfile medicalProfile;
}