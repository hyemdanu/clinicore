package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medication_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationInventory {

    @Id
    private Long id;


    @Column(name = "dosage_per_serving", length = 100)
    private String dosagePerServing;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
