package com.clinicore.project.repository;

import com.clinicore.project.entity.ResidentCaregiver;
import com.clinicore.project.entity.ResidentCaregiverId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResidentCaregiverRepository extends JpaRepository<ResidentCaregiver, ResidentCaregiverId> {

    // give me all residents assigned to this caregiver
    List<ResidentCaregiver> findById_CaregiverId(Long caregiverId);

    List<ResidentCaregiver> findById_ResidentId(Long residentId);
}
