package com.clinicore.project.repository;

import com.clinicore.project.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    /**
     * Load all medications with their medical profile in one query.
     * Avoids N+1 when grouping medications by resident (getMedicalProfile() is LAZY).
     */
    @Query("SELECT m FROM Medication m LEFT JOIN FETCH m.medicalProfile")
    List<Medication> findAllWithMedicalProfile();

    /**
     * List all medications for a resident (via medical profile)
     */
    List<Medication> findByMedicalProfileResidentId(Long residentId);

    /**
     * Load a single medication with its medical profile in one query.
     * Avoids lazy load when accessing getMedicalProfile().getResidentId() for permission checks.
     */
    @Query("SELECT m FROM Medication m JOIN FETCH m.medicalProfile WHERE m.id = :id")
    Optional<Medication> findByIdWithProfile(@Param("id") Long id);

    /**
     * Query which medication is in medication inventory; for inventory tracking
     */
    List<Medication> findByMedicationInventoryId(Long medicationInventoryId);

    /**
     * Query medications by intake status for all residents; 
     * will use later for listing which medications need to be administered
     */
    List<Medication> findByIntakeStatus(Medication.IntakeStatus intakeStatus);

    /**
     * Query medications by intake status for a specific resident; 
     * will use later for listing which medications need to be administered to a resident
     */
    List<Medication> findByMedicalProfileResidentIdAndIntakeStatus(
            Long residentId, 
            Medication.IntakeStatus intakeStatus
    );
}