package com.clinicore.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.clinicore.project.entity.MedicalRecord;
import com.clinicore.project.entity.Medication;
import com.clinicore.project.entity.Resident;

import java.util.List;

@Repository
public interface ResidentHealthRecordRepo extends JpaRepository<Resident, Long> {
    
    // Fetch medical record for a specific resident
    MedicalRecord findMedicalRecordsByResidentId(Integer residentId);

    // Fetch all medications for a specific resident
    List<Medication> findMedicationsByResidentId(Integer residentId);
}
