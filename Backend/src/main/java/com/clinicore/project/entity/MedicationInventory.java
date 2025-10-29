package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medication_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationInventory {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Item item;

    @Column(name = "dosage_per_serving", length = 100)
    private String dosagePerServing;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Inverse side - medications that reference this inventory item
    @OneToMany(mappedBy = "medicationInventory", fetch = FetchType.LAZY)
    private List<Medication> medications = new ArrayList<>();
}
