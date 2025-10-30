
package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_consumables")
public class MedicalConsumable {

    @Id
    private Long id;

    // Relationship to item (medical consumable is a child of item; item has 2 childs either medication or medical consumable)
    // MedicalConsumable --> 1:1 --> Item, joined by column id
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Item item;
}