package com.clinicore.project.repository;

import com.clinicore.project.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryRepository extends JpaRepository<Item, Long> {

    /** Case-insensitive search for items whose name contains a substring. */
    List<Item> findByNameContainingIgnoreCase(String namePart);

    /** Find items with quantity at or below a certain threshold (low stock query). */
    List<Item> findByQuantityLessThanEqual(Integer quantity);

    /** Get all items from a specific supplier. */
    List<Item> findBySupplierId(Long supplierId);

}
