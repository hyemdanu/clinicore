// Edison Ho
// Resident General Controller
package com.clinicore.project.controller;

// imports
import com.clinicore.project.entity.Resident;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.ResidentGeneralRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

// annotations
@RestController
@RequestMapping("/api/resident")
@CrossOrigin
public class ResidentGeneralController {

    // variables
    private final UserProfileRepository userProfileRepository;
    private final ResidentGeneralRepository residentRepository;

    // constructor that injects both repositories so the controller can access user and resident data
    public ResidentGeneralController(UserProfileRepository userProfileRepository,
                                     ResidentGeneralRepository residentRepository) {
        this.userProfileRepository = userProfileRepository;
        this.residentRepository = residentRepository;
    }

    /**
     * This will take the ID connected to the resident from userProfile and then pulls the general information
     * (like name) and then combines it with the resident information table (like emergency contact).
     * Returns a JSON object containing both user profile and resident info
     */
    @GetMapping("/{userProfileId}/general")
    public ResponseEntity<?> getResidentGeneral(@PathVariable Long userProfileId) {
        // find the user profile by ID
        UserProfile up = userProfileRepository.findById(userProfileId).orElse(null);
        // error handling
        if (up == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "UserProfile not found", "userProfileId", userProfileId));
        }

        // find the resident using the same ID it found earlier
        Resident res = residentRepository.findById(userProfileId).orElse(null);
        if (res == null) {
            // error handling
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Resident not found for userProfileId", "userProfileId", userProfileId));
        }

        // hash map to store all the info together
        Map<String, Object> residentsResult = new LinkedHashMap<>();
        residentsResult.put("userProfileId", userProfileId);

        // basic user profile details
        residentsResult.put("username", up.getUsername());
        residentsResult.put("gender", up.getGender());
        residentsResult.put("birthday", up.getBirthday());
        residentsResult.put("contactNumber", up.getContactNumber());

        // additional resident-specific information
        residentsResult.put("emergencyContactName", res.getEmergencyContactName());
        residentsResult.put("emergencyContactNumber", res.getEmergencyContactNumber());
        residentsResult.put("medicalProfileId", res.getMedicalProfileId());
        residentsResult.put("notes", res.getNotes());

        // return the hashmapped result
        return ResponseEntity.ok(residentsResult);
    }
}
