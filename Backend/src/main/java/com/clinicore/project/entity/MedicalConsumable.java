package com.clinicore.project.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medical_consumables")
public class MedicalConsumable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    // Default constructor (required by JPA)
    public MedicalConsumable() {
    }
    
    // Constructor with parameters
    public MedicalConsumable(String name, Integer quantity) {
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
    
    // Utility methods for increment/decrement
    public void incrementQuantity() {
        if (this.quantity != null) {
            this.quantity++;
        } else {
            this.quantity = 1;
        }
    }
    
    public void decrementQuantity() {
        if (this.quantity != null && this.quantity > 0) {
            this.quantity--;
        }
    }
    
    // toString method for debugging
    @Override
    public String toString() {
        return "MedicalConsumable{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}