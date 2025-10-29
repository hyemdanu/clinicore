package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "capability")
public class Capability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean verbal;
    private Boolean self_medicates;
    
    @Enumerated(EnumType.STRING)
    private IncontinenceStatus incontinence_status;
    
    @Enumerated(EnumType.STRING)
    private MobilityStatus mobility_status;
    
    @Column(updatable = false)
    private LocalDateTime created_at;
    
    private LocalDateTime updated_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }

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
}