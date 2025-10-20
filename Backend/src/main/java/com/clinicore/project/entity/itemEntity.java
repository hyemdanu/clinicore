package com.clinicore.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item")
public class ItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category") // e.g., medication, supply, device
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "sku") // Optional internal code or barcode
    private String sku;

    @Column(name = "lot_number") // For traceability/recalls
    private String lotNumber;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "supplier_id") // Keeping FK as a simple ID like other entities
    private Long supplierId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit") // e.g., tablets, vials, boxes
    private String unit;

    @Column(name = "unit_price") // Use BigDecimal for money
    private BigDecimal unitPrice;

    @Column(name = "expiration_date") // Useful for meds/sterile supplies
    private LocalDate expirationDate;

    @Column(name = "location") // Storage location (e.g., "Pharmacy A - Shelf 3")
    private String location;

    @Column(name = "reorder_threshold") // When to trigger restock
    private Integer reorderThreshold;

    @Column(name = "notes")
    private String notes;
}
