package com.techready.backend.controller;

import com.techready.backend.model.InterviewQuestion;
import com.techready.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public ResponseEntity<List<InterviewQuestion>> getQuestions(
            @RequestParam(required = false) String filiere,
            @RequestParam(required = false) String specialite,
            @RequestParam(required = false) String niveau,
            @RequestParam(required = false) Boolean isVocalAccessible,
            @RequestParam(required = false) String entrepriseTag) {

        if (filiere == null || filiere.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            List<InterviewQuestion> questions = questionService.getQuestionsByCriteria(filiere, specialite, niveau);
            
            if (questions.size() < 5 && niveau != null && !niveau.trim().isEmpty()) {
                // Relance sans le filtre niveau
                questions = questionService.getQuestionsByCriteria(filiere, specialite, null);
            }
            
            if (questions.size() < 5 && specialite != null && !specialite.trim().isEmpty()) {
                // Relance avec uniquement la filière (Fallback total)
                questions = questionService.getQuestionsByCriteria(filiere, null, null);
            }

            if (questions.size() < 5) {
                // Fallback ultime absolu : on renvoie toutes les questions de la base au hasard
                // Utile si l'utilisateur a choisi "Informatique générale" (qui n'a aucune question précise)
                questions = questionService.getQuestionsByCriteria(null, null, null);
            }
            
            // Appliquer les nouveaux filtres en mémoire pour éviter les erreurs d'index Firestore
            if (isVocalAccessible != null || entrepriseTag != null) {
                List<InterviewQuestion> filtered = new java.util.ArrayList<>();
                for (InterviewQuestion q : questions) {
                    boolean keep = true;
                    if (isVocalAccessible != null && !Boolean.TRUE.equals(q.getIsVocalAccessible())) {
                        keep = false;
                    }
                    if (entrepriseTag != null && !entrepriseTag.equalsIgnoreCase(q.getEntrepriseTag())) {
                        keep = false;
                    }
                    if (keep) {
                        filtered.add(q);
                    }
                }
                questions = filtered;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Filter-Relaxed", "true");
            
            return new ResponseEntity<>(questions, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
