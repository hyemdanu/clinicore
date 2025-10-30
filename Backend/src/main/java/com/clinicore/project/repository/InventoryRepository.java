package com.clinicore.project.repository;

import java.time.LocalDate;
import java.util.List;

import com.clinicore.project.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Item, Long> {

    /** Get all items supplied by a specific supplier. */
    List<Item> findBySupplierId(Long supplierId);

    /** Get all items within a given category (e.g., "medication", "supply", "device"). */
    List<Item> findByCategory(String category);

    /** Case-insensitive search for items whose name contains a substring. */
    List<Item> findByNameContainingIgnoreCase(String namePart);

    /** Find an item by its SKU (useful for uniqueness checks or scanning). */
    Item findBySku(String sku);

    /** Get all items from a specific manufacturer. */
    List<Item> findByManufacturer(String manufacturer);

    /** Find items with a specific lot number (traceability/recalls). */
    List<Item> findByLotNumber(String lotNumber);

    /** Find items that expire before a given date (already expired if date is today). */
    List<Item> findByExpirationDateBefore(LocalDate date);

    /** Find items expiring on or before a given date (useful for near-expiry reports). */
    List<Item> findByExpirationDateLessThanEqual(LocalDate date);

    /** Find items at or below a given quantity (low stock query). */
    List<Item> findByQuantityLessThanEqual(Integer quantity);

    /** Delete all items provided by a specific supplier (e.g., supplier removed). */
    void deleteBySupplierId(Long supplierId);

    /** Items needing reorder: quantity <= reorder_threshold. */
    @Query("""
           SELECT i FROM Item i
           WHERE i.reorderThreshold IS NOT NULL
             AND i.quantity IS NOT NULL
             AND i.quantity <= i.reorderThreshold
           """)
    List<Item> findItemsNeedingReorder();

    /** Convenience: get supplier's items ordered by name. */
    List<Item> findBySupplierIdOrderByNameAsc(Long supplierId);
}
