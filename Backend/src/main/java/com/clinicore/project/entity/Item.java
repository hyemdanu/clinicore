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
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "category") // e.g., medication, supply, device
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "sku")
    private String sku;

    @Column(name = "lot_number")
    private String lotNumber;

    @Column(name = "manufacturer")
    private String manufacturer;

    @Column(name = "supplier_id")
    private Long supplierId; // FK kept as simple ID, matching team style

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit") // e.g., tablets, vials, boxes
    private String unit;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "location")
    private String location;

    @Column(name = "reorder_threshold")
    private Integer reorderThreshold;

    @Column(name = "notes")
    private String notes;
}
