package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "medical_profile_id")
    private Long medicalProfileId;

    @Column(name = "medication_inventory_id")
    private Long medicationInventoryId;

    @Column(name = "medication_name")
    private String medicationName;

    @Column
    private String dosage;

    @Column
    private String frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "intake_status")
    private IntakeStatus intakeStatus;

    @Column(name = "last_administered_at")
    private LocalDateTime lastAdministeredAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    public boolean isTrackedInInventory() {
        return medicationInventoryId != null;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum IntakeStatus {
        ADMINISTERED,
        WITHHELD,
        MISSED,
        PENDING
    }
}