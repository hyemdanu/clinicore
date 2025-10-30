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
    @Column(name = "resident_id")
    private Long residentId;

    @Column(name = "hospice_agency")
    private String hospiceAgency;
    
    @Column(name = "preferred_hospital")
    private String preferredHospital;
    
    @Column(name = "preferred_pharmacy")
    private String preferredPharmacy;
    
    @Column(name = "home_health_agency")
    private String homeHealthAgency;
    
    private String mortuary;
    
    @Column(name = "dnr_polst")
    private String dnrPolst;
    
    private Boolean hospice;
    
    @Column(name = "home_health")
    private Boolean homeHealth;
    
    private String notes;

    // Relationship is owned by MedicalProfile (the owning side is the medical services property in MedicalProfile)
    // MedicalProfile controls the FK via @JoinColumn
    @OneToOne(mappedBy = "medicalServices")
    private MedicalProfile medicalProfile;
}
