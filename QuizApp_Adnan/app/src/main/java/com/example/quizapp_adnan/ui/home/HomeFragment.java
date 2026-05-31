package com.example.quizapp_adnan.ui.home;

import android.graphics.Color;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.data.model.SkillDTO;
import com.example.quizapp_adnan.ui.interview.InterviewActivity;
import com.example.quizapp_adnan.ui.mock.MockInterviewActivity;
import com.example.quizapp_adnan.ui.profiling.ProfilingActivity;
import com.example.quizapp_adnan.ui.survival.SurvivalActivity;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private HomeViewModel viewModel;
    private TextView tvWelcome, tvLevel, tvPanicMode, tvNoStats, tvXpPercent;
    private ProgressBar pbXp;
    private RadarChart radarChart;
    private MaterialCardView btnQuick, btnVocal, btnSurvival, btnMockInterview, btnFlashcards;

    private boolean userHasProfile = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvLevel = view.findViewById(R.id.tvLevel);
        tvPanicMode = view.findViewById(R.id.tvPanicMode);
        tvNoStats = view.findViewById(R.id.tvNoStats);
        pbXp = view.findViewById(R.id.pbXp);
        radarChart = view.findViewById(R.id.radarChart);
        tvXpPercent = view.findViewById(R.id.tvXpPercent);

        btnQuick = view.findViewById(R.id.btnQuick);
        btnVocal = view.findViewById(R.id.btnVocal);
        btnSurvival = view.findViewById(R.id.btnSurvival);
        btnMockInterview = view.findViewById(R.id.btnMockInterview);
        btnFlashcards = view.findViewById(R.id.btnFlashcards);

        setupObservers();
        setupListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadUserData();
        viewModel.fetchSkills();
    }

    private void setupObservers() {
        viewModel.getCurrentUserData().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                tvWelcome.setText("Bonjour " + user.getDisplayName() + " 👋"); 
                
                int xp = user.getTotalSessions() * 50;
                String levelStr;
                int maxProgress;
                if (xp <= 200)      { levelStr = "Débutant";   maxProgress = 200; }
                else if (xp <= 500) { levelStr = "Junior Dev"; maxProgress = 500; }
                else if (xp <= 1000){ levelStr = "Mid Dev";    maxProgress = 1000; }
                else                { levelStr = "Senior Dev"; maxProgress = xp + 500; }

                tvLevel.setText(levelStr);           // badge — texte court uniquement
                pbXp.setMax(maxProgress);
                pbXp.setProgress(xp);
                int percent = maxProgress > 0 ? (int)((xp * 100f) / maxProgress) : 0;
                if (tvXpPercent != null) tvXpPercent.setText(percent + "%");
            }
        });

        viewModel.getHasProfile().observe(getViewLifecycleOwner(), hasProfile -> {
            this.userHasProfile = hasProfile;
        });

        viewModel.getSkillsData().observe(getViewLifecycleOwner(), skills -> {
            tvNoStats.setVisibility(View.GONE);
            radarChart.setVisibility(View.VISIBLE);
            if (skills != null && !skills.isEmpty()) {
                setupRadarChart(skills);
            } else {
                // Afficher des valeurs de démonstration statiques si pas encore de données réelles
                List<SkillDTO> demoSkills = new ArrayList<>();
                demoSkills.add(new SkillDTO("Backend", 75));
                demoSkills.add(new SkillDTO("Frontend", 60));
                demoSkills.add(new SkillDTO("SQL", 45));
                demoSkills.add(new SkillDTO("Algo", 50));
                demoSkills.add(new SkillDTO("Git", 80));
                setupRadarChart(demoSkills);
            }
        });

        viewModel.getPanicMode().observe(getViewLifecycleOwner(), isPanic -> {
            tvPanicMode.setVisibility(isPanic ? View.VISIBLE : View.GONE);
        });
    }

    private void setupRadarChart(List<SkillDTO> skills) {
        ArrayList<RadarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        for (SkillDTO skill : skills) {
            float value = Math.max(1f, skill.getPourcentage()); // éviter les valeurs zéro qui cachent la forme
            entries.add(new RadarEntry(value));
            labels.add(skill.getCategorie());
        }

        // --- Dataset ---
        RadarDataSet dataSet = new RadarDataSet(entries, "Compétences");
        dataSet.setColor(Color.parseColor("#3A7CBD"));        // bordure visible
        dataSet.setFillColor(Color.parseColor("#5B9BD5"));    // remplissage plus saturé
        dataSet.setDrawFilled(true);
        dataSet.setFillAlpha(130);                            // plus opaque
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightCircleEnabled(true);
        dataSet.setDrawHighlightIndicators(false);

        // --- Chart global ---
        radarChart.getDescription().setEnabled(false);
        radarChart.getLegend().setEnabled(false);
        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(Color.LTGRAY);
        radarChart.setWebLineWidthInner(1f);
        radarChart.setWebColorInner(Color.LTGRAY);
        radarChart.setWebAlpha(100);

        // --- Axe X (labels des catégories) ---
        XAxis xAxis = radarChart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.parseColor("#2D2D3A"));
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        // --- Axe Y (valeurs 0-100) ---
        YAxis yAxis = radarChart.getYAxis();
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);
        yAxis.setDrawLabels(false);

        // --- Données + animation ---
        RadarData data = new RadarData(dataSet);
        data.setDrawValues(false);
        radarChart.setData(data);
        radarChart.animateXY(1400, 1400, Easing.EaseInOutQuad);
        radarChart.invalidate();
    }

    private void setupListeners() {
        btnQuick.setOnClickListener(v -> {
            if (userHasProfile) {
                startActivity(new Intent(requireContext(), InterviewActivity.class));
            } else {
                startActivity(new Intent(requireContext(), ProfilingActivity.class));
            }
        });

        // Raccourci de test : Un appui long permet de refaire le profilage !
        btnQuick.setOnLongClickListener(v -> {
            startActivity(new Intent(requireContext(), ProfilingActivity.class));
            return true;
        });
        
        btnFlashcards.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.flashcardsFragment);
        });

        btnSurvival.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), SurvivalActivity.class));
        });

        btnMockInterview.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), MockInterviewActivity.class));
        });

        btnVocal.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), com.example.quizapp_adnan.ui.interview.VocalModeActivity.class));
        });

        // Other buttons
    }
}
