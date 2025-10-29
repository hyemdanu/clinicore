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

    @Column
    private Long medical_profile_id;

    @Column
    private Long medication_inventory_id;

    @Column(name = "medication_name")
    private String medicationName;

    @Column
    private String dosage;

    @Column
    private String frequency;

    @Enumerated(EnumType.STRING)
    private IntakeStatus intake_status;

    private LocalDateTime last_administered_at;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false)
    private LocalDateTime created_at;

    private LocalDateTime updated_at;

    @Transient
    public boolean isTrackedInInventory() {
        return medication_inventory_id != null;
    }

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }

    public enum IntakeStatus {
        ADMINISTERED,
        WITHHELD,
        MISSED,
        PENDING
    }
}