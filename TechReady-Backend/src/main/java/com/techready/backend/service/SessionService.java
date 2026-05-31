package com.techready.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.techready.backend.model.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class SessionService {

    private static final String SESSIONS_COLLECTION = "sessions";
    private static final String USERS_COLLECTION = "profiles";

    @Autowired
    private Firestore firestore;

    public String saveSession(Session session) throws ExecutionException, InterruptedException {
        // 1. Calcul du pourcentage côté serveur
        if (session.getTotal() != null && session.getTotal() > 0) {
            double percentage = ((double) session.getScore() / session.getTotal()) * 100.0;
            session.setPercentage(percentage);
        } else {
            session.setPercentage(0.0);
        }

        if (session.getDate() == null) {
            session.setDate(new java.util.Date());
        }

        // 2. Utilisation d'un batch pour ajouter la session ET mettre à jour le user en même temps
        WriteBatch batch = firestore.batch();

        DocumentReference sessionRef = firestore.collection(SESSIONS_COLLECTION).document();
        batch.set(sessionRef, session);

        DocumentReference userRef = firestore.collection(USERS_COLLECTION).document(session.getUserId());
        DocumentSnapshot userSnapshot = userRef.get().get();
        
        if (userSnapshot.exists()) {
            Long currentBestScore = userSnapshot.getLong("bestScore");
            if (currentBestScore == null) currentBestScore = 0L;
            
            Long currentTotalSessions = userSnapshot.getLong("totalSessions");
            if (currentTotalSessions == null) currentTotalSessions = 0L;

            Long currentTotalPoints = userSnapshot.getLong("totalPoints");
            if (currentTotalPoints == null) currentTotalPoints = 0L;

            int newBestScore = Math.max(currentBestScore.intValue(), session.getScore());
            int newTotalSessions = currentTotalSessions.intValue() + 1;
            int pointsEarned = (int) (session.getPercentage() * 10);
            int newTotalPoints = currentTotalPoints.intValue() + pointsEarned;

            batch.update(userRef, "bestScore", newBestScore);
            batch.update(userRef, "totalSessions", newTotalSessions);
            batch.update(userRef, "totalPoints", newTotalPoints);
        }

        // Exécution du batch
        batch.commit().get();

        return sessionRef.getId();
    }

    public List<Session> getUserSessions(String userId) throws ExecutionException, InterruptedException {
        CollectionReference sessions = firestore.collection(SESSIONS_COLLECTION);
        // Nécessite un index composé sur (userId, date) dans Firestore
        Query query = sessions.whereEqualTo("userId", userId).orderBy("date", Query.Direction.DESCENDING);
        
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Session> resultList = new ArrayList<>();
        
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            Session session = document.toObject(Session.class);
            if (session != null) {
                resultList.add(session);
            }
        }
        return resultList;
    }
}
