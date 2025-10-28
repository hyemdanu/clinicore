package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resident")
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emergency_contact_name", nullable = false)
    private String emergencyContactName;

    @Column(name = "emergency_contact_number", nullable = false)
    private String emergencyContactNumber;

    @Column
    private String notes;

    //
    @OneToOne(mappedBy = "resident", cascade = CascadeType.ALL, orphanRemoval = true)
    private MedicalProfile medicalProfile;
}
