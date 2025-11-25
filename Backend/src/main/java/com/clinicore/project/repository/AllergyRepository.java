package com.clinicore.project.repository;

import com.clinicore.project.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, Long> {

    /**
     * Find all allergies for a specific resident
     */
    List<Allergy> findByResidentId(Long residentId);

    /**
     * Find allergies by severity level
     */
    List<Allergy> findBySeverity(Integer severity);

    /**
     * Find allergies by resident and severity
     */
    List<Allergy> findByResidentIdAndSeverity(Long residentId, Integer severity);
}
