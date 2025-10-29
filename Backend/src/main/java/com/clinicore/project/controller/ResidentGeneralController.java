// Edison Ho
// Resident General Controller (Uses both UserProfileRepository and ResidentGeneralRepository
// ResidentGeneral and UserProfile is connected since ResidentGeneral just extends UserProfile

package com.clinicore.project.controller;

// imports
import com.clinicore.project.entity.Resident;
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.ResidentGeneralRepository;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * Get a list of all residents (for caregiver to browse/select)
     * Returns basic info only - just enough to display in a list
     * 
     * @return JSON array containing basic info for all residents
     */
    @GetMapping("/list")
    public ResponseEntity<?> getAllResidents() {
        List<UserProfile> allUsers = userProfileRepository.findAll();
        List<Map<String, Object>> residentList = new ArrayList<>();
        
        for (UserProfile user : allUsers) {
            // Check if this user is also a resident
            Optional<Resident> resident = residentRepository.findById(user.getId());
            
            if (resident.isPresent()) {
                // This user is a resident, add to list
                Map<String, Object> residentInfo = new LinkedHashMap<>();
                residentInfo.put("id", user.getId());
                residentInfo.put("firstName", user.getFirstName());
                residentInfo.put("lastName", user.getLastName());
                
                residentList.add(residentInfo);
            }
        }
        
        return ResponseEntity.ok(residentList);
    }

    /**
     * This will take the ID connected to the resident from userProfile and then pulls the general information
     * (like name) and then combines it with the resident information table (like emergency contact).
     * 
     * Used by:
     * - Residents to view their own information
     * - Caregivers/Admins to view a selected resident's detailed information
     * 
     * @param userProfileId The ID of the user/resident to retrieve
     * @return JSON object containing both user profile and resident info
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
        residentsResult.put("firstName", up.getFirstName());
        residentsResult.put("lastName", up.getLastName());
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
