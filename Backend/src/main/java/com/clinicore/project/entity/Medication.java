package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medication")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Every medication belongs to a medical profile
    // All residents have a medical profile
    // A medical profile can have multiple medications (meaning a resident can have multiple medications)
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "medical_profile_id", nullable = true)
    private MedicalProfile medicalProfile;

    // Medication may or may not be tracked in inventory
    // If tracked in inventory, it has a medication inventory
    // Some medications might be homemade or brought by family
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "medication_inventory_id", nullable = true)
    private MedicationInventory medicationInventory;

    @Column(name = "medication_name", nullable = false)
    private String medicationName;

    @Column
    private String dosage;

    @Column
    private String frequency;

    @Enumerated(EnumType.STRING)
    @Column(name = "intake_status")
    private IntakeStatus intakeStatus = IntakeStatus.PENDING;

    @Column(name = "last_administered_at")
    private LocalDateTime lastAdministeredAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // to check if medication is tracked in inventory
    @Transient
    public boolean isTrackedInInventory() {
        return medicationInventory != null;
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