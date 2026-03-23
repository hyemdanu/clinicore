package com.clinicore.project.repository;

import com.clinicore.project.entity.MedicationInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MedicationInventoryRepository extends JpaRepository<MedicationInventory, Long> {

    /** 
     * Join with item table to get name and quantity.
     * Since MedicationInventory shares id with Item.
     */
    @Query("""
           SELECT mi FROM MedicationInventory mi
           JOIN Item i ON mi.id = i.id
           ORDER BY i.name ASC
           """)
    List<MedicationInventory> findAllOrderedByName();

    /**
     * Only return medications at or below the given stock threshold.
     */
    @Query("""
           SELECT mi FROM MedicationInventory mi
           JOIN Item i ON mi.id = i.id
           WHERE i.quantity <= :threshold
           ORDER BY i.name ASC
           """)
    List<MedicationInventory> findLowStock(@Param("threshold") Integer threshold);

}
