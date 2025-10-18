package com.clinicore.project.entity;

//imports
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

//annotations
@Entity
@Table(name = "medication")
@Getter
@Setter
@NoArgsConstructor

public class MedicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private double dosage;

    @Column
    private boolean intake_status;

    @Column
    private String notes;


}


