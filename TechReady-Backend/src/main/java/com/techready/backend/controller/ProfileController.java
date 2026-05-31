package com.techready.backend.controller;

import com.techready.backend.model.UserProfile;
import com.techready.backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @PostMapping
    public ResponseEntity<String> createProfile(@Valid @RequestBody UserProfile profile) {
        try {
            String profileId = profileService.createProfile(profile);
            return ResponseEntity.status(HttpStatus.CREATED).body(profileId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{userId}/last")
    public ResponseEntity<UserProfile> getLastProfile(@PathVariable String userId) {
        try {
            UserProfile profile = profileService.getProfile(userId);
            if (profile != null) {
                return ResponseEntity.ok(profile);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/skills")
    public ResponseEntity<java.util.List<com.techready.backend.model.SkillDTO>> getSkills(@RequestParam String userId) {
        try {
            java.util.List<com.techready.backend.model.SkillDTO> skills = profileService.getSkills(userId);
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<java.util.List<UserProfile>> getLeaderboard(@RequestParam String filiere) {
        try {
            java.util.List<UserProfile> leaderboard = profileService.getLeaderboard(filiere);
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
