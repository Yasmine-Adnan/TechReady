package com.example.quizapp_adnan.ui.result;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.history.HistoryActivity;
import com.example.quizapp_adnan.ui.home.HomeActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

public class ResultActivity extends AppCompatActivity {

    private ResultViewModel viewModel;

    // Views
    private ScoreRingView scoreRingView;
    private TextView tvPerformanceLabel, tvMessage;
    private ProgressBar pbAILoading;
    private ImageButton ibBack, ibShare;
    private android.widget.Button bHome, bHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        viewModel = new ViewModelProvider(this).get(ResultViewModel.class);

        // Bind views
        scoreRingView       = findViewById(R.id.scoreRingView);
        tvPerformanceLabel  = findViewById(R.id.tvPerformanceLabel);
        tvMessage           = findViewById(R.id.tvMessage);
        pbAILoading         = findViewById(R.id.pbAILoading);
        ibBack              = findViewById(R.id.ibBack);
        ibShare             = findViewById(R.id.ibShare);
        bHome               = findViewById(R.id.bHome);
        bHistory            = findViewById(R.id.bHistory);

        setupObservers();
        setupListeners();

        // ── All existing data loading — UNCHANGED ──
        viewModel.loadLastSession();
    }

    // ────────────────────────────────────────────────────────────
    //  Observers  (API + Gemini logic completely untouched)
    // ────────────────────────────────────────────────────────────

    private void setupObservers() {

        viewModel.getLastSession().observe(this, session -> {
            if (session != null) {
                int percentage = (int) session.getPercentage();

                // ── Performance label (Section 2) ──
                applyPerformanceLabel(percentage);

                // ── Animate circular ring 0 → percentage over 1000ms (Section 1) ──
                animateRing(percentage);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getIsLoadingAi().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                pbAILoading.setVisibility(View.VISIBLE);
                tvMessage.setText("Génération du feedback personnalisé en cours...");
            } else {
                pbAILoading.setVisibility(View.GONE);
            }
        });

        // ── Gemini feedback — kept exactly as before ──
        viewModel.getAiFeedback().observe(this, feedback -> {
            if (feedback != null && !feedback.isEmpty()) {
                tvMessage.setText(feedback);
            }
        });
    }

    // ────────────────────────────────────────────────────────────
    //  Section 1 — Animate the ring with ValueAnimator
    // ────────────────────────────────────────────────────────────

    private void animateRing(int targetPct) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, targetPct);
        animator.setDuration(1000);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(anim -> {
            float value = (float) anim.getAnimatedValue();
            scoreRingView.setProgress(value);
        });
        animator.start();
    }

    // ────────────────────────────────────────────────────────────
    //  Section 2 — Performance label text + color
    // ────────────────────────────────────────────────────────────

    private void applyPerformanceLabel(int percentage) {
        String label;
        int color;

        if (percentage <= 40) {
            label = "Aïe... 😬";
            color = 0xFFE53935;   // red
        } else if (percentage <= 60) {
            label = "Pas mal ! 💪";
            color = 0xFFFB8C00;   // orange
        } else if (percentage <= 80) {
            label = "Bien joué ! 😊";
            color = 0xFF43A047;   // green
        } else {
            label = "Excellent ! 🎉";
            color = 0xFF1E88E5;   // blue/indigo
        }

        tvPerformanceLabel.setText(label);
        tvPerformanceLabel.setTextColor(color);
    }

    // ────────────────────────────────────────────────────────────
    //  Listeners
    // ────────────────────────────────────────────────────────────

    private void setupListeners() {

        // Back arrow
        ibBack.setOnClickListener(v -> {
            onBackPressed();
        });

        // Share (placeholder — no logic needed per spec)
        ibShare.setOnClickListener(v -> {
            Snackbar.make(v, "Partage à venir", Snackbar.LENGTH_SHORT).show();
        });

        // "Reprendre la pratique" → navigate to HomeActivity (existing logic)
        bHome.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // "Voir le rapport détaillé" → HistoryActivity (existing logic)
        bHistory.setOnClickListener(v -> {
            startActivity(new Intent(ResultActivity.this, HistoryActivity.class));
            finish();
        });
    }
}
