package com.techready.backend.controller;

import com.techready.backend.model.Session;
import com.techready.backend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @PostMapping
    public ResponseEntity<String> createSession(@Valid @RequestBody Session session) {
        try {
            String sessionId = sessionService.saveSession(session);
            return ResponseEntity.status(HttpStatus.CREATED).body(sessionId);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Session>> getUserSessions(@PathVariable String userId) {
        try {
            List<Session> sessions = sessionService.getUserSessions(userId);
            if (sessions != null && !sessions.isEmpty()) {
                return ResponseEntity.ok(sessions);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
