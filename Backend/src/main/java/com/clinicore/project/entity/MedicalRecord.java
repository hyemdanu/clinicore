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

    // Relationship is owned by MedicalProfile (the owning side is the medical record property in MedicalProfile)
    // MedicalProfile controls the FK via @JoinColumn
    @OneToOne(mappedBy = "medicalRecord")
    private MedicalProfile medicalProfile;

    // Helper Methods for Allergies since these are lists of strings
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
        this.allergy = allergies == null || allergies.isEmpty() 
            ? null 
            : String.join(", ", allergies);
    }

    @Transient
    public void addAllergy(String allergyItem) {
        List<String> allergies = new java.util.ArrayList<>(getAllergyList());
        if (!allergies.contains(allergyItem)) {
            allergies.add(allergyItem);
            setAllergyList(allergies);
        }
    }

    @Transient
    public void removeAllergy(String allergyItem) {
        List<String> allergies = new java.util.ArrayList<>(getAllergyList());
        allergies.remove(allergyItem);
        setAllergyList(allergies);
    }


    // Helper Methods for Diagnosis since these are lists of strings
    @Transient
    public List<String> getDiagnosisList() {
        if (diagnosis == null || diagnosis.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(diagnosis.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    @Transient
    public void setDiagnosisList(List<String> diagnosisList) {
        this.diagnosis = diagnosisList == null || diagnosisList.isEmpty()
                ? null
                : String.join(", ", diagnosisList);
    }

    @Transient
    public void addDiagnosis(String diagnosisItem) {
        List<String> diagnosisList = new java.util.ArrayList<>(getDiagnosisList());
        if (!diagnosisList.contains(diagnosisItem)) {
            diagnosisList.add(diagnosisItem);
            setDiagnosisList(diagnosisList);  
        }
    }

    @Transient
    public void removeDiagnosis(String diagnosisItem) {
        List<String> diagnosisList = new java.util.ArrayList<>(getDiagnosisList());
        diagnosisList.remove(diagnosisItem);
        setDiagnosisList(diagnosisList);
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