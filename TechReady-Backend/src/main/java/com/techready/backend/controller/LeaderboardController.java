package com.techready.backend.controller;

import com.techready.backend.model.CampusRankResponse;
import com.techready.backend.model.CampusRequest;
import com.techready.backend.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    @Autowired
    private CampusService campusService;

    /**
     * POST /api/leaderboard/campus
     * Body: { "userId": "...", "latitude": 33.97, "longitude": -6.84 }
     * Retourne le rang du user sur le campus le plus proche (< 500m)
     * ou 404 si aucun campus TechReady détecté à proximité.
     */
    @PostMapping("/campus")
    public ResponseEntity<CampusRankResponse> getCampusRank(@RequestBody CampusRequest request) {
        try {
            CampusRankResponse response = campusService.getCampusRank(request);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
