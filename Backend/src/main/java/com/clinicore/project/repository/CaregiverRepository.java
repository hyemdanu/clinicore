package com.clinicore.project.repository;

import com.clinicore.project.entity.Caregiver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CaregiverRepository extends JpaRepository<Caregiver, Long> {
}