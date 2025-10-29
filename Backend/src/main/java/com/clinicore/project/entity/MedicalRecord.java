package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_record")
public class MedicalRecord {

    @Id
    @OneToOne
    @JoinColumn(name = "resident_id", nullable = false) // Foreign key to MedicalProfile
    private Resident resident;

    @Column // Allow null, possible no allergy
    private String allergy;

    @Column
    private String diagnosis;

    private String notes;

    
}
