package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalServices {

    @Id
    private Long resident_id;

    private String hospice_agency;
    private String preferred_hospital;
    private String preferred_pharmacy;
    private String home_health_agency;
    private String mortuary;
    private String dnr_polst;
    private Boolean hospice;
    private Boolean home_health;
    private String notes;

    // Owned side - relationship to MedicalProfile
    @OneToOne
    @MapsId
    @JoinColumn(name = "resident_id")
    private MedicalProfile medicalProfile;
}
