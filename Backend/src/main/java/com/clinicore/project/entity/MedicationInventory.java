package com.clinicore.project.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medication_inventory")
public class MedicationInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int quantity;

    // Constructors
    public MedicationInventory() {}

    public MedicationInventory(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
