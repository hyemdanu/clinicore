package com.clinicore.project.entity;

//imports
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//annotations
@Entity
@Table(name = "medical_services")
@Getter
@Setter
@NoArgsConstructor

public class MedicalServices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column
    private String hospice_agency;

    @Column
    private String preferred_hospital;

    @Column
    private String preferred_pharmacy;

    @Column
    private String home_health_agency;

    @Column
    private String mortuary;

    @Column
    private String dnr_polst;

    @Column
    private Boolean hospice;

    @Column
    private Boolean home_health;

    @Column
    private String notes;
}
