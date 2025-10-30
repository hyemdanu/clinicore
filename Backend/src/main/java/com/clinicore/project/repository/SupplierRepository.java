package com.clinicore.project.repository;

import com.clinicore.project.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    /** Find supplier by exact name. */
    Supplier findByName(String name);

    /** Case-insensitive search for suppliers whose name contains a substring. */
    List<Supplier> findByNameContainingIgnoreCase(String namePart);

}