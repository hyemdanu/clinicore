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
    @Column(name = "resident_id")
    private Long residentId;

    @Column
    private String insurance;

    private String notes;
    
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 1:1 relationships - owned by related entities
    @OneToOne(mappedBy = "medicalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private Capability capability;

    @OneToOne(mappedBy = "medicalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private MedicalServices medicalServices;
    
    @OneToOne(mappedBy = "medicalProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private MedicalRecord medicalRecord;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
