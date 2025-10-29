package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_profile")
public class MedicalProfile {

    @Id
    @Column(name = "resident_id")
    private Long residentId;

    @Column
    private String insurance;

    @Column
    private String notes;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // 1:1 relationship with resident (1 resident : 1 medical profile), thus join by resID
    // MedicalProfile owns the relationship to Capability, MedicalServices, MedicalRecord, and Medications
    // When a medical profile is deleted, all related child entities delete
    // Capability, MedicalServices, and MedicalRecord are joined (or connected by) ResID
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "resident_id", referencedColumnName = "resident_id")
    private Capability capability;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "resident_id", referencedColumnName = "resident_id")
    private MedicalServices medicalServices;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "resident_id", referencedColumnName = "resident_id")
    private MedicalRecord medicalRecord;

    // 1:N relationship - child entities reference MedicalProfile
    // MedicalProfile owns the relationship to Medication
    // When a medical profile is deleted, all related child entities delete
    @OneToMany(mappedBy = "medicalProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Medication> medications = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
