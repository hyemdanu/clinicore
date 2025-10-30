package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "caregiver")
public class Caregiver {

    @Id
    private Long id;

    private String notes;

    // Relationship to user profile (caregiver is the owning side of the relationship)
    // Caregiver --> 1:1 -> UserProfile, joined by column id
    // @MapsId uses the FK as the PK, so no separate ID column is created (caregiver.id = userProfile.id)
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private UserProfile userProfile;
}