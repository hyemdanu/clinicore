package com.clinicore.project.repository;

import com.clinicore.project.entity.MedicalConsumable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface MedicalConsumableRepository extends JpaRepository<MedicalConsumable, Long> {

    /** 
     * Join with item table to get name and quantity.
     * Since MedicalConsumable only has id, we need to join with Item.
     */
    @Query("""
           SELECT mc FROM MedicalConsumable mc
           JOIN Item i ON mc.id = i.id
           ORDER BY i.name ASC
           """)
    List<MedicalConsumable> findAllOrderedByName();

    /**
     * Only return consumables at or below the given stock threshold.
     */
    @Query("""
           SELECT mc FROM MedicalConsumable mc
           JOIN Item i ON mc.id = i.id
           WHERE i.quantity <= :threshold
           ORDER BY i.name ASC
           """)
    List<MedicalConsumable> findLowStock(@Param("threshold") Integer threshold);

}