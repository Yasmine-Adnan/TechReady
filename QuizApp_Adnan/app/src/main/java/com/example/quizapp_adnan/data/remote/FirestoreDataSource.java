package com.example.quizapp_adnan.data.remote;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Singleton gérant l'instance globale de FirebaseFirestore.
 */
public class FirestoreDataSource {
    private static FirestoreDataSource instance;
    private final FirebaseFirestore db;

    private FirestoreDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirestoreDataSource getInstance() {
        if (instance == null) {
            instance = new FirestoreDataSource();
        }
        return instance;
    }

    public FirebaseFirestore getDb() {
        return db;
    }
}
