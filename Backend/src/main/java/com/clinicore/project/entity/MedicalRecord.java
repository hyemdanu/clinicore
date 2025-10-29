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
    private Long resident_id;

    @Column(columnDefinition = "TEXT")
    private String allergy;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(updatable = false)
    private LocalDateTime created_at;

    private LocalDateTime updated_at;

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
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }
}