package com.techready.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.techready.backend.model.Challenge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Service
public class ChallengeService {

    private static final String COLLECTION_NAME = "challenges";

    @Autowired
    private Firestore firestore;

    @PostConstruct
    public void initDefaultChallenge() {
        try {
            DocumentReference docRef = firestore.collection(COLLECTION_NAME).document("weeklyChallenge");
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
            if (!document.exists()) {
                Challenge defaultChallenge = new Challenge(
                        "weeklyChallenge",
                        "Défi Hebdomadaire",
                        "Réussir 3 entretiens techniques cette semaine avec plus de 80% de score.",
                        "Performance",
                        50,
                        "2026-05-17",
                        "2026-05-24",
                        "🎯"
                );
                docRef.set(defaultChallenge);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public Challenge getCurrentChallenge() throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document("weeklyChallenge");
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(Challenge.class);
        } else {
            return null;
        }
    }
}
