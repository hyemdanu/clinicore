package com.clinicore.project.dtos;

/**
 * Simplified DTO for displaying medical consumables in the frontend
 * Only includes fields needed for the inventory display
 */
public class MedicalConsumableDisplayDto {
    private Long id;
    private String name;
    private Integer quantity;
    
    // Default constructor
    public MedicalConsumableDisplayDto() {
    }
    
    // Constructor with all fields
    public MedicalConsumableDisplayDto(Long id, String name, Integer quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return "MedicalConsumableDisplayDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}