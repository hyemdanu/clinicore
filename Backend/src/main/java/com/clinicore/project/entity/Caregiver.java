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

    // Parent relationship required (caregiver IS a user)
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private UserProfile userProfile;
}