package com.clinicore.project.repository;

import com.clinicore.project.entity.MedicationInventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationInventoryRepository extends JpaRepository<MedicationInventory, Long> {
}
