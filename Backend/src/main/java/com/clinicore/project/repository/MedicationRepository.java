package com.clinicore.project.repository;

import com.clinicore.project.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    /**
     * List all medications for a resident (via medical profile)
     */
    List<Medication> findByMedicalProfileId(Long medicalProfileId);

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
    List<Medication> findByMedicalProfileIdAndIntakeStatus(
            Long medicalProfileId, 
            Medication.IntakeStatus intakeStatus
    );
}