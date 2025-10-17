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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column // Allow null, Possible to have no insurance?
    private String insurance;

    @Column(name = "medical_services_id", nullable = false)
    private Long medicalServicesId;

    @Column(name = "capabilities_id", nullable = false)
    private Long capabilitiesId;

    @Column(name = "medication_id") // Allow null, Patient may not need any medication
    private Long medicationId;

    @Column(name = "medical_record_id", nullable = false)
    private Long medicalRecordId;

    @Column
    private String notes; 
}
