package com.clinicore.project.repository;

import com.clinicore.project.entity.MedicationInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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

}
