package com.example.quizapp_adnan.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.auth.LoginActivity;
import com.example.quizapp_adnan.ui.onboarding.OnboardingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {
            // Forcer la mise à jour de la BDD à chaque lancement pour prendre en compte le Panic Mode
            new com.example.quizapp_adnan.data.remote.SeedDataManager().seedIfNeeded();

            SharedPreferences prefs = getSharedPreferences("techready_prefs", MODE_PRIVATE);
            boolean onboardingComplete = prefs.getBoolean("onboarding_complete", false);

            if (!onboardingComplete) {
                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
            } else {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
            }
            finish();
        }, 2000);
    }
}
