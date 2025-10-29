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
    @Column(name = "resident_id")
    private Long residentId;

    @Column
    private Boolean verbal;

    @Column(name = "self_medicates")
    private Boolean selfMedicates;

    @Enumerated(EnumType.STRING)
    @Column(name = "incontinence_status")
    private IncontinenceStatus incontinenceStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "mobility_status")
    private MobilityStatus mobilityStatus;

    // Relationship is owned by MedicalProfile (the owning side is the capability property in MedicalProfile)
    // MedicalProfile controls the FK via @JoinColumn
    @OneToOne(mappedBy = "capability")
    private MedicalProfile medicalProfile; // allows accesss to related MedicalProfile from this capability object

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
}