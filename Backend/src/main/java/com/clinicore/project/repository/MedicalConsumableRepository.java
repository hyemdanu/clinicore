package com.clinicore.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.clinicore.project.entity.MedicalConsumable;

@Repository
public interface MedicalConsumableRepository extends JpaRepository<MedicalConsumable, Long> {
    
    /**
     * Find medical consumables by name (case-insensitive)
     * Spring Data JPA automatically implements this method!
     */
    List<MedicalConsumable> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find medical consumables with low quantity (below specified threshold)
     * Useful for inventory alerts
     */
    List<MedicalConsumable> findByQuantityLessThan(Integer quantity);
    
    /**
     * Find medical consumables by exact name match
     */
    List<MedicalConsumable> findByName(String name);
    
    /**
     * Check if a medical consumable with the given name exists
     * Useful for validation when creating new items
     */
    boolean existsByName(String name);
}