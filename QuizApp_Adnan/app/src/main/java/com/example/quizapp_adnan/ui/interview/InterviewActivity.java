package com.example.quizapp_adnan.ui.interview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.result.ResultActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class InterviewActivity extends AppCompatActivity {
    private InterviewViewModel viewModel;

    private TextView tvProgress, tvTimer, tvQuestion;
    private ProgressBar progressBar;
    private RadioGroup rgOptions;
    private Button bSubmit;

    // Éléments de l'Overlay d'explication
    private LinearLayout layoutExplanation;
    private TextView tvExplanationTitle, tvExplanationText;
    private Button bNextQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview);

        viewModel = new ViewModelProvider(this).get(InterviewViewModel.class);

        tvProgress = findViewById(R.id.tvProgress);
        tvTimer = findViewById(R.id.tvTimer);
        progressBar = findViewById(R.id.progressBar);
        tvQuestion = findViewById(R.id.tvQuestion);
        rgOptions = findViewById(R.id.rgOptions);
        bSubmit = findViewById(R.id.bSubmit);

        layoutExplanation = findViewById(R.id.layoutExplanation);
        tvExplanationTitle = findViewById(R.id.tvExplanationTitle);
        tvExplanationText = findViewById(R.id.tvExplanationText);
        bNextQuestion = findViewById(R.id.bNextQuestion);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getQuestionProgress().observe(this, progress -> {
            tvProgress.setText("Question " + progress + "/5");
            progressBar.setProgress(progress);
        });

        viewModel.getTimeLeft().observe(this, time -> {
            int minutes = time / 60;
            int seconds = time % 60;
            // Affiche rouge si moins de 30 secondes
            if (time < 30) {
                tvTimer.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            } else {
                tvTimer.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
            }
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
        });

        viewModel.getCurrentQuestion().observe(this, question -> {
            if (question != null) {
                tvQuestion.setText(question.getQuestion());
                rgOptions.removeAllViews();
                List<String> options = question.getOptions();

                // On peuple le RadioGroup dynamiquement
                for (int i = 0; i < options.size(); i++) {
                    RadioButton rb = new RadioButton(this);
                    rb.setId(i); // L'ID devient l'index de la réponse, ce qui est très pratique
                    rb.setText(options.get(i));
                    rb.setTextSize(16);
                    rb.setPadding(0, 16, 0, 16);
                    rgOptions.addView(rb);
                }

                bSubmit.setEnabled(true);
                rgOptions.clearCheck();
                layoutExplanation.setVisibility(View.GONE);

                // Réactiver les boutons radio
                for (int i = 0; i < rgOptions.getChildCount(); i++) {
                    rgOptions.getChildAt(i).setEnabled(true);
                }
            }
        });

        viewModel.getExplanationData().observe(this, data -> {
            if (data != null) {
                // Afficher l'overlay d'explication
                layoutExplanation.setVisibility(View.VISIBLE);
                bSubmit.setEnabled(false);

                // Désactiver les boutons pour empêcher le changement de réponse
                for (int i = 0; i < rgOptions.getChildCount(); i++) {
                    rgOptions.getChildAt(i).setEnabled(false);
                }

                if (data.isCorrect) {
                    tvExplanationTitle.setText("Excellente réponse !");
                    tvExplanationTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                } else {
                    tvExplanationTitle.setText("Aïe, mauvaise réponse !");
                    tvExplanationTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));

                    // Mettre en surbrillance la bonne réponse
                    RadioButton correctRb = findViewById(data.correctIndex);
                    if (correctRb != null) {
                        correctRb.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
                    }
                }
                tvExplanationText.setText(data.explanation);
            } else {
                layoutExplanation.setVisibility(View.GONE);
            }
        });

        viewModel.getIsFinished().observe(this, isFinished -> {
            if (isFinished != null && isFinished) {
                // L'entretien est fini et sauvegardé !
                Intent intent = new Intent(InterviewActivity.this, ResultActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        bSubmit.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Snackbar.make(v, "Veuillez sélectionner une réponse", Snackbar.LENGTH_SHORT).show();
                return;
            }
            viewModel.submitAnswer(selectedId);
        });

        bNextQuestion.setOnClickListener(v -> {
            viewModel.nextQuestion();
        });
    }
}
