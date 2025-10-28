package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_profile")
public class MedicalProfile {

    @Id
    @OneToOne
    @JoinColumn(name = "resident_id", nullable = false, unique = true) // Foreign key to Resident
    private Resident resident; // Primary Key

    @Column // Allow null; Possible to have no insurance?
    private String insurance;

    @Column(name = "medical_services_id", nullable = false)
    private Long medicalServicesId;

    @Column(name = "capabilities_id", nullable = false)
    private Long capabilitiesId;

    private String notes;

    @OneToOne(mappedBy = "medicalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private MedicalRecord medicalRecord;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true) // Unidirectional relationship to medication
    private List<Medication> medications; // List of medications associated with the resident
}
