package com.example.quizapp_adnan.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.example.quizapp_adnan.data.repository.UserRepository;
import com.google.firebase.firestore.FieldValue;

public class BadgeManager {

    private static final UserRepository userRepository = new UserRepository();

    public static void checkAndAwardBadge(Context context, String userId, String badgeKey, String badgeName) {
        if (userId == null || userId.isEmpty() || context == null) return;

        // On vérifie d'abord si l'utilisateur possède déjà ce badge
        userRepository.getUser(userId).addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                java.util.List<String> badges = (java.util.List<String>) documentSnapshot.get("badges");
                if (badges == null || !badges.contains(badgeKey)) {
                    // L'utilisateur n'a pas le badge, on l'ajoute
                    awardBadge(context, userId, badgeKey, badgeName);
                }
            }
        }).addOnFailureListener(e -> {
            // Ignorer silencieusement en cas d'erreur réseau pour ne pas gêner l'UX
        });
    }

    private static void awardBadge(Context context, String userId, String badgeKey, String badgeName) {
        // Utilisation de arrayUnion pour ajouter à la liste existante côté serveur sans tout écraser
        com.example.quizapp_adnan.data.remote.FirestoreDataSource.getInstance()
                .getDb()
                .collection("users")
                .document(userId)
                .update("badges", FieldValue.arrayUnion(badgeKey))
                .addOnSuccessListener(aVoid -> {
                    // Afficher le Toast sur le Main Thread
                    new Handler(Looper.getMainLooper()).post(() -> 
                        Toast.makeText(context, "🏅 Badge débloqué : " + badgeName, Toast.LENGTH_LONG).show()
                    );
                });
    }
}
