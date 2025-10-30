package com.clinicore.project.entity;

// imports
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin")
public class Admin {

    @Id
    private Long id;

    // Relationship to user profile (admin is the owning side of the relationship)
    // Admin --> 1:1 -> UserProfile, joined by column id
    // @MapsId uses the FK as the PK, so no separate ID column is created (admin.id = userProfile.id)
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private UserProfile userProfile;
}
