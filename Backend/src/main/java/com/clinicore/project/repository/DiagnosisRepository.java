package com.clinicore.project.repository;

import com.clinicore.project.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    /**
     * Find all diagnoses for a specific resident
     */
    List<Diagnosis> findByResidentId(Long residentId);
}
