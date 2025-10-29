package com.clinicore.project.repository;

import com.clinicore.project.entity.MedicalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalProfileRepository extends JpaRepository<MedicalProfile, Long> {
}