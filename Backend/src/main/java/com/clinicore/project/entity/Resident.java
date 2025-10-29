package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resident")
public class Resident {

    @Id
    private Long id;

    @Column(name = "emergency_contact_name", nullable = false)
    private String emergencyContactName;

    @Column(name = "emergency_contact_number", nullable = false)
    private String emergencyContactNumber;

    @Column
    private String notes;
    
    @Column(updatable = false)
    private LocalDateTime created_at;
    
    private LocalDateTime updated_at;

    // Relationship to user profile (resident is the owning side of the relationship)
    // resident --> 1:1 -> UserProfile, joined by column id
    // @MapsId uses the FK as the PK, so no separate ID column is created (resident.id = userProfile.id)
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private UserProfile userProfile;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}