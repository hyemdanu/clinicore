package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_profile")
public class MedicalProfile {

    @Id
    private Long resident_id;

    @Column
    private String insurance;

    private String notes;
    
    @Column(updatable = false)
    private LocalDateTime created_at;
    
    private LocalDateTime updated_at;

    // 1:1 relationships - owned by related entities
    @OneToOne(mappedBy = "medicalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Capability capability;

    @OneToOne(mappedBy = "medicalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private MedicalServices medicalServices;

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
