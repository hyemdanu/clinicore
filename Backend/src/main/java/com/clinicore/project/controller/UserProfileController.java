// Olei Amelie Ngan
// This is the User Profile Controller class, which is used to return
// information about the logged-in user (user is either caregiver or admin)
// Side note: This is not the authentication controller for
// Side note: for residents user, residents extends user repositories/entities (fields) -> shares ID

package com.clinicore.project.controller;

//imports
import com.clinicore.project.entity.UserProfile;
import com.clinicore.project.repository.UserProfileRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.Map;

//annotations  // Fixed spelling
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserProfileController {

    // variables
    private final UserProfileRepository userProfileRepository;

    // constructor that injects the repository so the controller can access user data
    public UserProfileController(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * This will take the ID connected to the user to reference and pull all the user
     * profile information, such as name.
     *
     * @param userProfileId
     * @return JSON object containing user profile information
     */
    @GetMapping("/{userProfileId}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userProfileId) {
        // find the user profile by ID

        UserProfile up = userProfileRepository.findById(userProfileId).orElse(null);

        // error handling
        if (up == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Userprofile not found", "userProfileId", userProfileId));
        }

        // hash map to store all the info together
        Map<String, Object> userResult = new LinkedHashMap<>();

        // place user information results in hashmap to return
        userResult.put("userProfileId", userProfileId);
        userResult.put("username", up.getUsername());

        userResult.put("firstName", up.getFirstName());
        userResult.put("lastName", up.getLastName());
        userResult.put("gender", up.getGender());
        userResult.put("birthday", up.getBirthday());
        userResult.put("contactNumber", up.getContactNumber());

        return ResponseEntity.ok(userResult);

    }







}



