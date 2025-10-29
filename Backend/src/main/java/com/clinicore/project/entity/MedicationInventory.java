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

    @Column(length = 100)
    private String dosage_per_serving;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
