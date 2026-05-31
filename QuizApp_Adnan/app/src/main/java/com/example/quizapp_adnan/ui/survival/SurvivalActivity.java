package com.example.quizapp_adnan.ui.survival;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class SurvivalActivity extends AppCompatActivity {

    private SurvivalViewModel viewModel;

    // Header
    private TextView tvScore, tvTimer, tvQuestion;
    private ProgressBar pbTimer;
    private ImageView heart1, heart2, heart3;

    // Question
    private RadioGroup rgOptions;
    private Button bSubmit;

    // Overlay explication
    private LinearLayout layoutExplanation;
    private TextView tvExplanationTitle, tvExplanationText;
    private Button bNextQuestion;

    // Overlay Game Over
    private FrameLayout layoutGameOver;
    private TextView tvFinalScore;
    private Button bRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survival);

        viewModel = new ViewModelProvider(this).get(SurvivalViewModel.class);

        // Header
        tvScore  = findViewById(R.id.tvScore);
        tvTimer  = findViewById(R.id.tvTimer);
        pbTimer  = findViewById(R.id.pbTimer);
        heart1   = findViewById(R.id.heart1);
        heart2   = findViewById(R.id.heart2);
        heart3   = findViewById(R.id.heart3);

        // Question
        tvQuestion = findViewById(R.id.tvQuestion);
        rgOptions  = findViewById(R.id.rgOptions);
        bSubmit    = findViewById(R.id.bSubmit);

        // Overlay explication
        layoutExplanation  = findViewById(R.id.layoutExplanation);
        tvExplanationTitle = findViewById(R.id.tvExplanationTitle);
        tvExplanationText  = findViewById(R.id.tvExplanationText);
        bNextQuestion      = findViewById(R.id.bNextQuestion);

        // Overlay Game Over
        layoutGameOver = findViewById(R.id.layoutGameOver);
        tvFinalScore   = findViewById(R.id.tvFinalScore);
        bRetry         = findViewById(R.id.bRetry);

        setupObservers();
        setupListeners();
    }

    // ─────────────────────────────────────────────────
    // Observers — AUCUNE logique métier modifiée
    // ─────────────────────────────────────────────────
    private void setupObservers() {

        viewModel.getScore().observe(this, score -> {
            tvScore.setText(String.valueOf(score));
        });

        viewModel.getLivesLeft().observe(this, lives -> {
            heart1.setImageResource(lives >= 1 ? R.drawable.ic_heart : R.drawable.ic_heart_broken);
            heart2.setImageResource(lives >= 2 ? R.drawable.ic_heart : R.drawable.ic_heart_broken);
            heart3.setImageResource(lives >= 3 ? R.drawable.ic_heart : R.drawable.ic_heart_broken);
        });

        viewModel.getTimeLeft().observe(this, time -> {
            tvTimer.setText(String.valueOf(time));
            pbTimer.setProgress(time);

            // Timer rouge néon si <= 5 secondes
            if (time <= 5) {
                tvTimer.setTextColor(Color.parseColor("#FF4444"));
                tvTimer.setShadowLayer(10f, 0f, 0f, Color.parseColor("#FF4444"));
            } else {
                tvTimer.setTextColor(Color.parseColor("#00D4FF"));
                tvTimer.setShadowLayer(10f, 0f, 0f, Color.parseColor("#00D4FF"));
            }
        });

        viewModel.getCurrentQuestion().observe(this, question -> {
            if (question != null) {
                tvQuestion.setText(question.getQuestion());
                rgOptions.removeAllViews();
                List<String> options = question.getOptions();

                for (int i = 0; i < options.size(); i++) {
                    RadioButton rb = new RadioButton(this);
                    rb.setId(i);
                    rb.setText(options.get(i));
                    rb.setTextSize(15f);
                    rb.setTextColor(Color.WHITE);
                    rb.setButtonTintList(
                        android.content.res.ColorStateList.valueOf(Color.WHITE)
                    );
                    rb.setPadding(0, 14, 0, 14);
                    rgOptions.addView(rb);
                }

                bSubmit.setEnabled(true);
                rgOptions.clearCheck();
                layoutExplanation.setVisibility(View.GONE);

                for (int i = 0; i < rgOptions.getChildCount(); i++) {
                    rgOptions.getChildAt(i).setEnabled(true);
                }
            }
        });

        viewModel.getExplanationData().observe(this, data -> {
            if (data != null) {
                layoutExplanation.setVisibility(View.VISIBLE);
                bSubmit.setEnabled(false);

                for (int i = 0; i < rgOptions.getChildCount(); i++) {
                    rgOptions.getChildAt(i).setEnabled(false);
                }

                if (data.isCorrect) {
                    tvExplanationTitle.setText("Survie continue !");
                    tvExplanationTitle.setTextColor(Color.parseColor("#22C55E"));
                } else {
                    tvExplanationTitle.setText("Aïe, une vie en moins !");
                    tvExplanationTitle.setTextColor(Color.parseColor("#FF4444"));

                    RadioButton correctRb = rgOptions.findViewById(data.correctIndex);
                    if (correctRb != null) {
                        correctRb.setTextColor(Color.parseColor("#22C55E"));
                    }
                }
                tvExplanationText.setText(data.explanation);
            } else {
                layoutExplanation.setVisibility(View.GONE);
            }
        });

        viewModel.getIsGameOver().observe(this, isGameOver -> {
            if (isGameOver != null && isGameOver) {
                showGameOverOverlay();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // ─────────────────────────────────────────────────
    // Listeners
    // ─────────────────────────────────────────────────
    private void setupListeners() {
        bSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Snackbar.make(v, "Veuillez sélectionner une réponse", Snackbar.LENGTH_SHORT).show();
                return;
            }
            viewModel.submitAnswer(selectedId);
        });

        bNextQuestion.setOnClickListener(v -> viewModel.nextQuestion());

        bRetry.setOnClickListener(v -> {
            // Recrée l'activité pour relancer une nouvelle partie
            recreate();
        });
    }

    // ─────────────────────────────────────────────────
    // Game Over — overlay au lieu de l'AlertDialog
    // ─────────────────────────────────────────────────
    private void showGameOverOverlay() {
        int finalScore = viewModel.getScore().getValue() != null
                ? viewModel.getScore().getValue() : 0;
        tvFinalScore.setText(String.valueOf(finalScore));
        layoutGameOver.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        // Si le Game Over est affiché, ferme juste l'overlay
        if (layoutGameOver != null && layoutGameOver.getVisibility() == View.VISIBLE) {
            finish();
            return;
        }
        new AlertDialog.Builder(this)
            .setTitle("Quitter le Mode Survie ?")
            .setMessage("Votre partie sera terminée et votre score ne sera pas sauvegardé. Êtes-vous sûr ?")
            .setPositiveButton("Oui", (dialog, which) -> super.onBackPressed())
            .setNegativeButton("Non", null)
            .show();
    }
}
