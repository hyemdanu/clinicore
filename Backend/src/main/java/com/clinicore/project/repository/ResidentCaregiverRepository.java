package com.clinicore.project.repository;

import com.clinicore.project.entity.ResidentCaregiver;
import com.clinicore.project.entity.ResidentCaregiverId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface ResidentCaregiverRepository extends JpaRepository<ResidentCaregiver, ResidentCaregiverId> {

    // give me all residents assigned to this caregiver
    List<ResidentCaregiver> findById_CaregiverId(Long caregiverId);

    List<ResidentCaregiver> findById_ResidentId(Long residentId);

    boolean existsByIdCaregiverIdAndIdResidentId(Long caregiverId, Long residentId);

    /**
     * Load all assignments with Resident→UserProfile and Caregiver→UserProfile in one query.
     * Without this, accessing getResident().getUserProfile() or getCaregiver().getUserProfile()
     * triggers 2+ lazy loads per assignment (N+1 on remote DB = seconds of latency).
     */
    @Query("SELECT rc FROM ResidentCaregiver rc " +
           "JOIN FETCH rc.resident r " +
           "JOIN FETCH r.userProfile " +
           "JOIN FETCH rc.caregiver c " +
           "JOIN FETCH c.userProfile")
    List<ResidentCaregiver> findAllWithProfiles();

    /**
     * Same as findAllWithProfiles but for a single resident.
     */
    @Query("SELECT rc FROM ResidentCaregiver rc " +
           "JOIN FETCH rc.resident r " +
           "JOIN FETCH r.userProfile " +
           "JOIN FETCH rc.caregiver c " +
           "JOIN FETCH c.userProfile " +
           "WHERE r.id = :residentId")
    List<ResidentCaregiver> findByResidentIdWithProfiles(@Param("residentId") Long residentId);

}
