package com.clinicore.project.controller;

import com.clinicore.project.entity.MedicationInventory;
import com.clinicore.project.repository.MedicationInventoryRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationInventoryController {

    private final MedicationInventoryRepository repo;

    public MedicationInventoryController(MedicationInventoryRepository repo) {
        this.repo = repo;
    }

    // Get all medications (table view)
    @GetMapping
    public List<MedicationInventory> getAll() {
        return repo.findAll();
    }

    // Get single medication (for 3-dots detail expansion)
    @GetMapping("/{id}")
    public MedicationInventory getOne(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() ->
                new RuntimeException("Medication not found with id " + id));
    }

    // Add new medication (plus button at top or bottom of list)
    @PostMapping
    public MedicationInventory create(@RequestBody MedicationInventory medication) {
        return repo.save(medication);
    }

    // Increment quantity (plus button per row)
    @PutMapping("/{id}/increment")
    public MedicationInventory incrementQuantity(@PathVariable Long id) {
        return repo.findById(id).map(med -> {
            med.setQuantity(med.getQuantity() + 1);
            return repo.save(med);
        }).orElseThrow(() -> new RuntimeException("Medication not found with id " + id));
    }

    // Decrement quantity (minus button per row)
    @PutMapping("/{id}/decrement")
    public MedicationInventory decrementQuantity(@PathVariable Long id) {
        return repo.findById(id).map(med -> {
            if (med.getQuantity() > 0) {
                med.setQuantity(med.getQuantity() - 1);
            }
            return repo.save(med);
        }).orElseThrow(() -> new RuntimeException("Medication not found with id " + id));
    }

    // Dummy endpoint for now (Will point to Medical Consumables page)
    @GetMapping("/next-page")
    public String goToNextPage() {
        return "Placeholder for Medical Consumables page";
    }
}
