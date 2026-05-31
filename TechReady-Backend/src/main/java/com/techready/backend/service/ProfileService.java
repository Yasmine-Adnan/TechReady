package com.techready.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.techready.backend.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class ProfileService {

    private static final String COLLECTION_NAME = "profiles";

    @Autowired
    private Firestore firestore;

    @Autowired
    private SessionService sessionService;

    public String createProfile(UserProfile profile) throws ExecutionException, InterruptedException {
        if (profile.getUserId() != null && !profile.getUserId().isEmpty()) {
            ApiFuture<WriteResult> result = firestore.collection(COLLECTION_NAME).document(profile.getUserId()).set(profile);
            result.get();
            return profile.getUserId();
        } else {
            ApiFuture<DocumentReference> addedDocRef = firestore.collection(COLLECTION_NAME).add(profile);
            return addedDocRef.get().getId();
        }
    }

    public UserProfile getProfile(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(UserProfile.class);
        } else {
            return null;
        }
    }

    public String updateProfile(String userId, UserProfile profile) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(userId);
        ApiFuture<WriteResult> result = docRef.set(profile);
        result.get();
        return result.get().getUpdateTime().toString();
    }

    public java.util.List<com.techready.backend.model.SkillDTO> getSkills(String userId) throws ExecutionException, InterruptedException {
        UserProfile profile = getProfile(userId);
        java.util.List<com.techready.backend.model.SkillDTO> skills = new java.util.ArrayList<>();
        if (profile == null) return skills;
        
        java.util.List<com.techready.backend.model.Session> sessions = sessionService.getUserSessions(userId);
        if (sessions.isEmpty()) {
            if (profile.getTechnos() != null) {
                for (String techno : profile.getTechnos()) {
                    skills.add(new com.techready.backend.model.SkillDTO(techno, 0));
                }
            }
            if(skills.isEmpty()) {
                skills.add(new com.techready.backend.model.SkillDTO("Général", 0));
            }
            return skills;
        }

        double totalScore = 0;
        int count = 0;
        for (com.techready.backend.model.Session s : sessions) {
            totalScore += s.getPercentage() != null ? s.getPercentage() : 0;
            count++;
        }
        int avg = count > 0 ? (int)(totalScore / count) : 0;

        if (profile.getTechnos() != null) {
            for (String techno : profile.getTechnos()) {
                skills.add(new com.techready.backend.model.SkillDTO(techno, avg));
            }
        }
        if(skills.isEmpty()) {
            skills.add(new com.techready.backend.model.SkillDTO("Général", avg));
        }
        return skills;
    }

    public java.util.List<UserProfile> getLeaderboard(String filiere) throws ExecutionException, InterruptedException {
        com.google.cloud.firestore.Query query = firestore.collection(COLLECTION_NAME)
                .whereEqualTo("filiere", filiere);
        ApiFuture<com.google.cloud.firestore.QuerySnapshot> querySnapshot = query.get();
        java.util.List<UserProfile> leaderboard = new java.util.ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            UserProfile profile = document.toObject(UserProfile.class);
            if (profile != null) {
                leaderboard.add(profile);
            }
        }
        
        // Tri en mémoire pour éviter l'erreur d'index manquant sur Firestore
        leaderboard.sort((p1, p2) -> Integer.compare(p2.getTotalPoints(), p1.getTotalPoints()));
                
        if (leaderboard.size() > 50) {
            leaderboard = leaderboard.subList(0, 50);
        }
        return leaderboard;
    }
}
