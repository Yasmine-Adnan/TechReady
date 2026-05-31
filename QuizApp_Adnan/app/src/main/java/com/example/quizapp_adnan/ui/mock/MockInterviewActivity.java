package com.example.quizapp_adnan.ui.mock;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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

public class MockInterviewActivity extends AppCompatActivity {
    private MockInterviewViewModel viewModel;
    
    private TextView tvProgress, tvGlobalTimer, tvQuestion;
    private ProgressBar pbGlobalTimer;
    private RadioGroup rgOptions;
    private Button bNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mock_interview);

        viewModel = new ViewModelProvider(this).get(MockInterviewViewModel.class);

        tvProgress = findViewById(R.id.tvProgress);
        tvGlobalTimer = findViewById(R.id.tvGlobalTimer);
        pbGlobalTimer = findViewById(R.id.pbGlobalTimer);
        tvQuestion = findViewById(R.id.tvQuestion);
        rgOptions = findViewById(R.id.rgOptions);
        bNext = findViewById(R.id.bNext);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getQuestionProgress().observe(this, progress -> {
            int total = viewModel.getTotalQuestions();
            tvProgress.setText("Question " + progress + "/" + total);
        });

        viewModel.getTimeLeft().observe(this, time -> {
            int minutes = time / 60;
            int seconds = time % 60;
            tvGlobalTimer.setText(String.format("%02d:%02d", minutes, seconds));
            pbGlobalTimer.setProgress(time);
            
            if (time < 60) {
                tvGlobalTimer.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
                pbGlobalTimer.setProgressTintList(android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(this, android.R.color.holo_red_dark)));
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
                    rb.setTextSize(16);
                    rb.setPadding(0, 16, 0, 16);
                    rgOptions.addView(rb);
                }
                
                bNext.setEnabled(true);
                rgOptions.clearCheck();
            }
        });

        viewModel.getIsFinished().observe(this, isFinished -> {
            if (isFinished != null && isFinished) {
                Intent intent = new Intent(MockInterviewActivity.this, ResultActivity.class);
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
        bNext.setOnClickListener(v -> {
            int selectedId = rgOptions.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Snackbar.make(v, "Veuillez sélectionner une réponse", Snackbar.LENGTH_SHORT).show();
                return;
            }
            viewModel.submitAnswer(selectedId);
        });
    }
    
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle("Abandonner l'entretien ?")
            .setMessage("Si vous quittez, l'entretien blanc sera annulé et ne sera pas sauvegardé.")
            .setPositiveButton("Oui", (dialog, which) -> super.onBackPressed())
            .setNegativeButton("Non", null)
            .show();
    }
}
