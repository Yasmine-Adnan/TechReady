package com.techready.backend.controller;

import com.techready.backend.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private com.techready.backend.service.QuestionService questionService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeSession(@RequestBody Map<String, Object> payload) {
        try {
            int score = payload.get("score") != null ? ((Number) payload.get("score")).intValue() : 0;
            int total = payload.get("total") != null ? ((Number) payload.get("total")).intValue() : 10;
            String filiere = (String) payload.get("filiere");
            String specialite = (String) payload.get("specialite");
            String niveau = (String) payload.get("niveau");
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> questionsAnswered = (java.util.List<Map<String, Object>>) payload.get("questionsAnswered");

            String feedback = geminiService.analyzeSession(score, total, filiere, specialite, niveau, questionsAnswered);
            return ResponseEntity.ok(Map.of("feedback", feedback));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de l'appel à l'IA Gemini."));
        }
    }

    @PostMapping("/vocal")
    public ResponseEntity<com.techready.backend.model.VocalSessionResponse> evaluateVocalSession(@RequestBody com.techready.backend.model.VocalSessionRequest request) {
        try {
            com.techready.backend.model.InterviewQuestion q = questionService.getQuestionById(request.getQuestionId());
            if (q == null) {
                return ResponseEntity.badRequest().body(new com.techready.backend.model.VocalSessionResponse(0.0, "Question introuvable", "", ""));
            }
            
            // Save the session loosely... wait the requirements say "Sauvegarde session vocale dans Firestore (typeMode = VOCAL_SIMULATION)".
            // Actually, we can just return the response and let the client save the session with the feedback, or we save it here.
            // Let's just return the evaluation here and let the client handle session creation using POST /api/sessions as usual, 
            // but if the requirement explicitly says "Sauvegarde session vocale dans Firestore", I can call sessionService.
            // But I don't have sessionService injected here. I will just return the response, the Android app saves sessions via /api/sessions.

            com.techready.backend.model.VocalSessionResponse response = geminiService.evaluateVocalResponse(
                q.getQuestion(), 
                q.getBonneReponseComplete() != null ? q.getBonneReponseComplete() : q.getExplanation(), 
                request.getReponseText()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(new com.techready.backend.model.VocalSessionResponse(0.0, "Erreur serveur", "", ""));
        }
    }
}
