package com.clinicore.project.controller;

import com.clinicore.project.dtos.ApiResponse;
import com.clinicore.project.dtos.MedicalConsumableDisplayDto;
import com.clinicore.project.entity.MedicalConsumable;
import com.clinicore.project.repository.MedicalConsumableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medical-consumables")
@CrossOrigin(origins = "http://localhost:4200") // Angular default port
public class MedicalConsumablesInventoryController {

    @Autowired
    private MedicalConsumableRepository medicalConsumableRepository;

    // GET - Get all medical consumables (name and quantity only)
    @GetMapping
    public ResponseEntity<ApiResponse<List<MedicalConsumableDisplayDto>>> getAllMedicalConsumables() {
        try {
            List<MedicalConsumable> consumables = medicalConsumableRepository.findAll();
            
            // Convert to display DTOs with only name and quantity
            List<MedicalConsumableDisplayDto> displayList = consumables.stream()
                .map(consumable -> new MedicalConsumableDisplayDto(
                    consumable.getId(),
                    consumable.getName(),
                    consumable.getQuantity()
                ))
                .collect(Collectors.toList());
            
            ApiResponse<List<MedicalConsumableDisplayDto>> response = new ApiResponse<>(1, displayList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<MedicalConsumableDisplayDto>> response = new ApiResponse<>(-1, "Error retrieving medical consumables");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // POST - Increment quantity (+ button)
    @PostMapping("/{id}/increment")
    public ResponseEntity<ApiResponse<MedicalConsumableDisplayDto>> incrementQuantity(@PathVariable Long id) {
        try {
            Optional<MedicalConsumable> optionalConsumable = medicalConsumableRepository.findById(id);
            
            if (optionalConsumable.isPresent()) {
                MedicalConsumable consumable = optionalConsumable.get();
                
                // Increment the quantity
                consumable.setQuantity(consumable.getQuantity() + 1);
                MedicalConsumable updatedConsumable = medicalConsumableRepository.save(consumable);
                
                // Return updated display DTO
                MedicalConsumableDisplayDto displayDto = new MedicalConsumableDisplayDto(
                    updatedConsumable.getId(),
                    updatedConsumable.getName(),
                    updatedConsumable.getQuantity()
                );
                
                ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(1, displayDto);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Medical consumable not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Error incrementing quantity");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // POST - Decrement quantity (- button)
    @PostMapping("/{id}/decrement")
    public ResponseEntity<ApiResponse<MedicalConsumableDisplayDto>> decrementQuantity(@PathVariable Long id) {
        try {
            Optional<MedicalConsumable> optionalConsumable = medicalConsumableRepository.findById(id);
            
            if (optionalConsumable.isPresent()) {
                MedicalConsumable consumable = optionalConsumable.get();
                
                // Prevent negative quantities
                if (consumable.getQuantity() > 0) {
                    consumable.setQuantity(consumable.getQuantity() - 1);
                    MedicalConsumable updatedConsumable = medicalConsumableRepository.save(consumable);
                    
                    // Return updated display DTO
                    MedicalConsumableDisplayDto displayDto = new MedicalConsumableDisplayDto(
                        updatedConsumable.getId(),
                        updatedConsumable.getName(),
                        updatedConsumable.getQuantity()
                    );
                    
                    ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(1, displayDto);
                    return ResponseEntity.ok(response);
                } else {
                    ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Quantity cannot be negative");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Medical consumable not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Error decrementing quantity");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // POST - Create new medical consumable (for adding test data)
    @PostMapping
    public ResponseEntity<ApiResponse<MedicalConsumableDisplayDto>> createMedicalConsumable(@RequestBody MedicalConsumable medicalConsumable) {
        try {
            // Validate required fields
            if (medicalConsumable.getName() == null || medicalConsumable.getName().trim().isEmpty() ||
                medicalConsumable.getQuantity() == null) {
                
                ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Invalid medical consumable data");
                return ResponseEntity.badRequest().body(response);
            }
            
            MedicalConsumable savedConsumable = medicalConsumableRepository.save(medicalConsumable);
            
            // Return display DTO
            MedicalConsumableDisplayDto displayDto = new MedicalConsumableDisplayDto(
                savedConsumable.getId(),
                savedConsumable.getName(),
                savedConsumable.getQuantity()
            );
            
            ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(1, displayDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Error creating medical consumable");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // GET - Get single medical consumable by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalConsumableDisplayDto>> getMedicalConsumableById(@PathVariable Long id) {
        try {
            Optional<MedicalConsumable> consumable = medicalConsumableRepository.findById(id);
            if (consumable.isPresent()) {
                MedicalConsumable entity = consumable.get();
                MedicalConsumableDisplayDto displayDto = new MedicalConsumableDisplayDto(
                    entity.getId(),
                    entity.getName(),
                    entity.getQuantity()
                );
                ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(1, displayDto);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Medical consumable not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Error retrieving medical consumable");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // PUT - Update medical consumable (for future use with 3-dots menu)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MedicalConsumableDisplayDto>> updateMedicalConsumable(
            @PathVariable Long id, @RequestBody MedicalConsumable medicalConsumableDetails) {
        try {
            Optional<MedicalConsumable> optionalConsumable = medicalConsumableRepository.findById(id);
            
            if (optionalConsumable.isPresent()) {
                MedicalConsumable existingConsumable = optionalConsumable.get();
                
                // Update fields if provided
                if (medicalConsumableDetails.getName() != null) {
                    existingConsumable.setName(medicalConsumableDetails.getName());
                }
                if (medicalConsumableDetails.getQuantity() != null) {
                    existingConsumable.setQuantity(medicalConsumableDetails.getQuantity());
                }
                
                MedicalConsumable updatedConsumable = medicalConsumableRepository.save(existingConsumable);
                
                MedicalConsumableDisplayDto displayDto = new MedicalConsumableDisplayDto(
                    updatedConsumable.getId(),
                    updatedConsumable.getName(),
                    updatedConsumable.getQuantity()
                );
                
                ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(1, displayDto);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Medical consumable not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<MedicalConsumableDisplayDto> response = new ApiResponse<>(-1, "Error updating medical consumable");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // DELETE - Delete medical consumable (for future use with 3-dots menu)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMedicalConsumable(@PathVariable Long id) {
        try {
            Optional<MedicalConsumable> optionalConsumable = medicalConsumableRepository.findById(id);
            
            if (optionalConsumable.isPresent()) {
                medicalConsumableRepository.deleteById(id);
                ApiResponse<String> response = new ApiResponse<>(1, "Medical consumable deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<String> response = new ApiResponse<>(-1, "Medical consumable not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            ApiResponse<String> response = new ApiResponse<>(-1, "Error deleting medical consumable");
            return ResponseEntity.badRequest().body(response);
        }
    }
}