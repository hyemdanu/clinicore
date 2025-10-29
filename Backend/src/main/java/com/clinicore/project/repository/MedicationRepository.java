package com.clinicore.project.repository;

import com.clinicore.project.entity.Medication;
import com.clinicore.project.entity.Medication.IntakeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {

    /**
     * These are the only usages i can think of right now but we'll add as we go...
     */

    // List all medications for a resident
    List<Medication> findByMedicalProfileId(Long medicalProfileId);


    // query which medication is in medication inventory; for inventory tracking
    List<Medication> findByMedicationInventoryId(Long medicationInventoryId);


    // query medications by intake status for all residents; will use later for listing which medications need to be administered
    List<Medication> findByIntakeStatusOrder(IntakeStatus intakeStatus);


    // query medications by intake status for a specific resident; will use later for listing which medications need to be administered to a resident
    List<Medication> findByMedicalProfileIdAndIntakeStatus(Long medicalProfileId, IntakeStatus intakeStatus);

}