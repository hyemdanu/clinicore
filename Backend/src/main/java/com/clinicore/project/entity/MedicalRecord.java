package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_record")
public class MedicalRecord {

    @Id
    @Column(name = "resident_id")
    private Long residentId;

    @Column(columnDefinition = "TEXT")
    private String allergy;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Owned side - relationship to MedicalProfile
    @OneToOne
    @MapsId
    @JoinColumn(name = "resident_id")
    private MedicalProfile medicalProfile;

    // Helper Methods for Multiple Allergies

    @Transient
    public List<String> getAllergyList() {
        if (allergy == null || allergy.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(allergy.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Transient
    public void setAllergyList(List<String> allergies) {
        if (allergies == null || allergies.isEmpty()) {
            this.allergy = null;
        } else {
            this.allergy = String.join(", ", allergies);
        }
    }

    @Transient
    public void addAllergy(String allergyItem) {
        List<String> allergies = getAllergyList().stream()
                .collect(Collectors.toList());
        if (!allergies.contains(allergyItem)) {
            allergies.add(allergyItem);
            setAllergyList(allergies);
        }
    }

    @Transient
    public void removeAllergy(String allergyItem) {
        List<String> allergies = getAllergyList().stream()
                .collect(Collectors.toList());
        allergies.remove(allergyItem);
        setAllergyList(allergies);
    }

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