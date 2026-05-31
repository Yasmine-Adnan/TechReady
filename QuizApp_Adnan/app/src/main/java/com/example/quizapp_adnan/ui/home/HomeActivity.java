package com.example.quizapp_adnan.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.auth.LoginActivity;
import com.example.quizapp_adnan.ui.history.HistoryActivity;
import com.example.quizapp_adnan.ui.interview.InterviewActivity;
import com.example.quizapp_adnan.ui.profiling.ProfilingActivity;
import com.google.android.material.snackbar.Snackbar;

public class HomeActivity extends AppCompatActivity {
    private HomeViewModel viewModel;
    private TextView tvWelcome, tvTotalSessions, tvBestScore;
    private Button bStartInterview, bLogout, bHistory;

    private boolean userHasProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalSessions = findViewById(R.id.tvTotalSessions);
        tvBestScore = findViewById(R.id.tvBestScore);
        bStartInterview = findViewById(R.id.bStartInterview);
        bLogout = findViewById(R.id.bLogout);
        bHistory = findViewById(R.id.bHistory);

        setupObservers();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // On recharge les données à chaque retour sur l'accueil
        // pour que les stats s'actualisent si on vient de finir un entretien
        viewModel.loadUserData();
    }

    private void setupObservers() {
        viewModel.getCurrentUserData().observe(this, user -> {
            if (user != null) {
                tvWelcome.setText("Bonjour, " + user.getDisplayName());
                tvTotalSessions.setText(String.valueOf(user.getTotalSessions()));
                tvBestScore.setText(user.getBestScore() + "%");
            }
        });

        viewModel.getHasProfile().observe(this, hasProfile -> {
            this.userHasProfile = hasProfile;
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        bLogout.setOnClickListener(v -> {
            viewModel.logout();
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        bStartInterview.setOnClickListener(v -> {
            Intent intent;
            if (userHasProfile) {
                // Le profil est complété, on lance l'entretien
                intent = new Intent(HomeActivity.this, InterviewActivity.class);
            } else {
                // Pas de profil détecté, on lance l'arbre de décision
                intent = new Intent(HomeActivity.this, ProfilingActivity.class);
            }
            startActivity(intent);
        });

        // Raccourci de test : Un appui long permet de refaire le profilage !
        bStartInterview.setOnLongClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfilingActivity.class));
            return true;
        });

        bHistory.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
        });
    }
}
