
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
}