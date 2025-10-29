package com.clinicore.project.repository;

import com.clinicore.project.entity.MedicalServices;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalServicesRepository extends JpaRepository<MedicalServices, Long> {

}