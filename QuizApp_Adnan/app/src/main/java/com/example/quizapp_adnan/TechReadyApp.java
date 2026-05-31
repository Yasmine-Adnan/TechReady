package com.example.quizapp_adnan;

import android.app.Application;

/**
 * Classe Application globale pour l'initialisation.
 * Note : La redirection (si currentUser != null -> HomeActivity) est une action UI.
 * Selon les bonnes pratiques Android, il est déconseillé de démarrer une Activité depuis 
 * Application.onCreate() car cela casse les Deep Links et les notifications. 
 * Cette redirection sera gérée proprement au lancement de LoginActivity (Étape 6).
 */
public class TechReadyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialisations globales (Firebase est déjà auto-initialisé)
    }
}
