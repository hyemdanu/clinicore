package com.clinicore.project.repository;

import com.clinicore.project.entity.MedicalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalProfileRepository extends JpaRepository<MedicalProfile, Long> {

    /**
     * Load all medical profiles with their OneToOne children and medications in one query.
     * Avoids N+1: without this, each findById triggers 3 extra queries (capability, services, record)
     * plus a lazy load for medications.
     */
    @Query("SELECT DISTINCT p FROM MedicalProfile p " +
           "LEFT JOIN FETCH p.capability " +
           "LEFT JOIN FETCH p.medicalServices " +
           "LEFT JOIN FETCH p.medicalRecord " +
           "LEFT JOIN FETCH p.medications m " +
           "LEFT JOIN FETCH m.medicationInventory mi " +
           "LEFT JOIN FETCH mi.item")
    List<MedicalProfile> findAllWithFullDetails();

    /**
     * Same as findAllWithFullDetails but for a single resident.
     */
    @Query("SELECT DISTINCT p FROM MedicalProfile p " +
           "LEFT JOIN FETCH p.capability " +
           "LEFT JOIN FETCH p.medicalServices " +
           "LEFT JOIN FETCH p.medicalRecord " +
           "LEFT JOIN FETCH p.medications m " +
           "LEFT JOIN FETCH m.medicationInventory mi " +
           "LEFT JOIN FETCH mi.item " +
           "WHERE p.residentId = :residentId")
    Optional<MedicalProfile> findByResidentIdWithFullDetails(@Param("residentId") Long residentId);
}