package com.example.quizapp_adnan.ui.profiling;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.data.model.ProfilingQuestion;
import com.example.quizapp_adnan.ui.interview.InterviewActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ProfilingActivity extends AppCompatActivity {
    private ProfilingViewModel viewModel;
    private TextView tvQuestion;
    private RadioGroup radioGroupOptions;
    private LinearLayout checkboxContainer;
    private Button bNext, bPrevious;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiling);

        viewModel = new ViewModelProvider(this).get(ProfilingViewModel.class);

        tvQuestion = findViewById(R.id.tvQuestion);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        checkboxContainer = findViewById(R.id.checkboxContainer);
        bNext = findViewById(R.id.bNext);
        bPrevious = findViewById(R.id.bPrevious);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getCurrentQuestion().observe(this, question -> {
            if (question != null) {
                displayQuestion(question);
            }
        });

        viewModel.getIsFinished().observe(this, isFinished -> {
            if (isFinished != null && isFinished) {
                boolean isEditMode = getIntent().getBooleanExtra("IS_EDIT_MODE", false);
                if (isEditMode) {
                    // L'utilisateur voulait juste modifier son profil, on ferme l'activité
                    finish();
                } else {
                    // Navigation par défaut (Onboarding) on passe à l'étape suivante (Entretien dynamique)
                    Intent intent = new Intent(ProfilingActivity.this, InterviewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void displayQuestion(ProfilingQuestion question) {
        tvQuestion.setText(question.getText());
        radioGroupOptions.removeAllViews();
        checkboxContainer.removeAllViews();

        Object previousAnswer = viewModel.getPreviousAnswerForCurrentQuestion();

        if (question.isMultiSelect()) {
            radioGroupOptions.setVisibility(View.GONE);
            checkboxContainer.setVisibility(View.VISIBLE);
            
            List<String> prevSelected = null;
            if (previousAnswer instanceof List) {
                prevSelected = (List<String>) previousAnswer;
            }

            for (String option : question.getOptions()) {
                CheckBox cb = new CheckBox(this);
                cb.setText(option);
                cb.setTextSize(16);
                cb.setPadding(0, 16, 0, 16);
                if (prevSelected != null && prevSelected.contains(option)) {
                    cb.setChecked(true);
                }
                checkboxContainer.addView(cb);
            }
        } else {
            radioGroupOptions.setVisibility(View.VISIBLE);
            checkboxContainer.setVisibility(View.GONE);
            
            String prevSelected = null;
            if (previousAnswer instanceof String) {
                prevSelected = (String) previousAnswer;
            }

            for (int i = 0; i < question.getOptions().size(); i++) {
                String option = question.getOptions().get(i);
                RadioButton rb = new RadioButton(this);
                rb.setId(View.generateViewId());
                rb.setText(option);
                rb.setTextSize(16);
                rb.setPadding(0, 16, 0, 16);
                radioGroupOptions.addView(rb);
                
                if (option.equals(prevSelected)) {
                    radioGroupOptions.check(rb.getId());
                }
            }
        }
    }

    private void setupListeners() {
        bNext.setOnClickListener(v -> {
            ProfilingQuestion current = viewModel.getCurrentQuestion().getValue();
            if (current == null) return;

            if (current.isMultiSelect()) {
                List<String> selectedOptions = new ArrayList<>();
                for (int i = 0; i < checkboxContainer.getChildCount(); i++) {
                    CheckBox cb = (CheckBox) checkboxContainer.getChildAt(i);
                    if (cb.isChecked()) {
                        selectedOptions.add(cb.getText().toString());
                    }
                }
                if (selectedOptions.isEmpty()) {
                    Snackbar.make(v, "Veuillez sélectionner au moins une option", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                viewModel.submitAnswer(selectedOptions);
            } else {
                int checkedId = radioGroupOptions.getCheckedRadioButtonId();
                if (checkedId == -1) {
                    Snackbar.make(v, "Veuillez sélectionner une option", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                RadioButton selectedRb = findViewById(checkedId);
                viewModel.submitAnswer(selectedRb.getText().toString());
            }
        });

        bPrevious.setOnClickListener(v -> {
            if (!viewModel.goBack()) {
                // Si on est à la racine, le retour annule l'opération (retour vers Home)
                finish();
            }
        });
    }

    // Gestion du bouton retour physique/swipe d'Android
    @Override
    public void onBackPressed() {
        if (!viewModel.goBack()) {
            super.onBackPressed();
        }
    }
}
