package com.example.quizapp_adnan.ui.progress;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.interview.InterviewActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class ProgressFragment extends Fragment {

    private ProgressViewModel viewModel;

    // ── Custom tab references (replaces TabLayout)
    private TextView tabEvolution, tabFaiblesses;

    // ── Content panels
    private LinearLayout layoutEvolution;
    private RecyclerView rvWeaknesses;

    // ── Chart
    private LineChart lineChart;

    // ── Motivation banner
    private MaterialCardView cardMotivation;
    private TextView tvMotivation;
    private ImageView ivTrendIcon;

    // ── Loading
    private ProgressBar pbLoading;

    // ── Weakness adapter
    private WeaknessAdapter adapter;

    // ── Active tab tracker (0 = évolution, 1 = faiblesses)
    private int activeTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        // Bind views
        tabEvolution    = view.findViewById(R.id.tabEvolution);
        tabFaiblesses   = view.findViewById(R.id.tabFaiblesses);
        layoutEvolution = view.findViewById(R.id.layoutEvolution);
        rvWeaknesses    = view.findViewById(R.id.rvWeaknesses);
        lineChart       = view.findViewById(R.id.lineChart);
        tvMotivation    = view.findViewById(R.id.tvMotivation);
        cardMotivation  = view.findViewById(R.id.cardMotivation);
        ivTrendIcon     = view.findViewById(R.id.ivTrendIcon);
        pbLoading       = view.findViewById(R.id.pbLoading);

        setupCustomTabs();
        setupChart();
        setupRecyclerView();
        setupObservers();

        // ── Data loading — UNCHANGED ──
        viewModel.fetchProgressData();
    }

    // ────────────────────────────────────────────────────────────
    //  Custom tab selector (replaces TabLayout)
    // ────────────────────────────────────────────────────────────

    private void setupCustomTabs() {
        // Default: tab 0 active
        setTabActive(0);

        tabEvolution.setOnClickListener(v -> setTabActive(0));
        tabFaiblesses.setOnClickListener(v -> setTabActive(1));
    }

    private void setTabActive(int tab) {
        activeTab = tab;

        if (tab == 0) {
            // Evolution tab active
            tabEvolution.setBackgroundResource(R.drawable.bg_tab_active);
            tabEvolution.setTextColor(Color.parseColor("#4A90D9"));
            tabEvolution.setTypeface(null, android.graphics.Typeface.BOLD);

            tabFaiblesses.setBackgroundResource(android.R.color.transparent);
            tabFaiblesses.setTextColor(Color.parseColor("#9CA3AF"));
            tabFaiblesses.setTypeface(null, android.graphics.Typeface.NORMAL);

            layoutEvolution.setVisibility(View.VISIBLE);
            rvWeaknesses.setVisibility(View.GONE);
        } else {
            // Faiblesses tab active
            tabFaiblesses.setBackgroundResource(R.drawable.bg_tab_active);
            tabFaiblesses.setTextColor(Color.parseColor("#4A90D9"));
            tabFaiblesses.setTypeface(null, android.graphics.Typeface.BOLD);

            tabEvolution.setBackgroundResource(android.R.color.transparent);
            tabEvolution.setTextColor(Color.parseColor("#9CA3AF"));
            tabEvolution.setTypeface(null, android.graphics.Typeface.NORMAL);

            layoutEvolution.setVisibility(View.GONE);
            rvWeaknesses.setVisibility(View.VISIBLE);
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Chart styling (target mockup spec)
    // ────────────────────────────────────────────────────────────

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setExtraBottomOffset(8f);

        // Y axis (left)
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setLabelCount(6, true);   // 0, 20, 40, 60, 80, 100
        leftAxis.setTextColor(Color.parseColor("#9CA3AF"));
        leftAxis.setTextSize(11f);
        leftAxis.setGridColor(Color.parseColor("#E5E7EB"));
        leftAxis.setGridLineWidth(1f);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGranularity(20f);

        // Y axis (right) — disabled
        lineChart.getAxisRight().setEnabled(false);

        // X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#9CA3AF"));
        xAxis.setTextSize(11f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.parseColor("#E5E7EB"));

        // Legend
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.SQUARE);
        legend.setFormSize(10f);
        legend.setTextColor(Color.parseColor("#1A1040"));
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
    }

    // ────────────────────────────────────────────────────────────
    //  RecyclerView — UNCHANGED logic
    // ────────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new WeaknessAdapter(category -> {
            Intent intent = new Intent(requireContext(), InterviewActivity.class);
            intent.putExtra("FILTER_CATEGORY", category);
            startActivity(intent);
        });
        rvWeaknesses.setAdapter(adapter);
    }

    // ────────────────────────────────────────────────────────────
    //  Observers — data logic UNCHANGED, UI layer updated
    // ────────────────────────────────────────────────────────────

    private void setupObservers() {

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            if (isLoading) {
                layoutEvolution.setVisibility(View.GONE);
                rvWeaknesses.setVisibility(View.GONE);
            } else {
                setTabActive(activeTab);   // restore active tab visibility
            }
        });

        // ── Motivation banner: dynamic color + icon based on trend ──
        viewModel.getMotivationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                // Strip emoji prefix, keep clean text for display
                String displayText = msg
                        .replace("📈 ", "")
                        .replace("📉 ", "");
                tvMotivation.setText(displayText);
                cardMotivation.setVisibility(View.VISIBLE);

                if (msg.startsWith("📈")) {
                    // Score en hausse → vert pastel
                    cardMotivation.setCardBackgroundColor(Color.parseColor("#B3F0D4"));
                    ivTrendIcon.setImageResource(android.R.drawable.stat_sys_upload);
                    ivTrendIcon.setColorFilter(Color.parseColor("#27AE60"));
                } else if (msg.startsWith("📉")) {
                    // Score en baisse → coral
                    cardMotivation.setCardBackgroundColor(Color.parseColor("#FFB3A0"));
                    ivTrendIcon.setImageResource(android.R.drawable.stat_sys_download);
                    ivTrendIcon.setColorFilter(Color.parseColor("#FF6B6B"));
                } else {
                    // Stable → bleu clair
                    cardMotivation.setCardBackgroundColor(Color.parseColor("#A8D8F0"));
                    ivTrendIcon.setImageResource(android.R.drawable.ic_menu_compass);
                    ivTrendIcon.setColorFilter(Color.parseColor("#4A90D9"));
                }
            } else {
                cardMotivation.setVisibility(View.GONE);
            }
        });

        // ── Chart data: apply all styling from spec ──
        viewModel.getChartData().observe(getViewLifecycleOwner(), entries -> {
            if (entries != null && !entries.isEmpty()) {
                LineDataSet dataSet = new LineDataSet(entries, "Score Moyen (%)");

                // Line style
                dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                dataSet.setColor(Color.parseColor("#4A90D9"));
                dataSet.setLineWidth(2.5f);

                // Fill below the curve
                dataSet.setDrawFilled(true);
                dataSet.setFillColor(Color.parseColor("#A8C8F8"));
                dataSet.setFillAlpha(80);

                // Data point circles
                dataSet.setDrawCircles(true);
                dataSet.setCircleColor(Color.parseColor("#4A90D9"));
                dataSet.setCircleRadius(5f);
                dataSet.setCircleHoleColor(Color.WHITE);
                dataSet.setCircleHoleRadius(2.5f);

                // Value labels above each point
                dataSet.setDrawValues(true);
                dataSet.setValueTextColor(Color.parseColor("#1A1040"));
                dataSet.setValueTextSize(11f);
                dataSet.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getPointLabel(Entry entry) {
                        return String.format("%.1f%%", entry.getY());
                    }
                });

                // Legend square color
                dataSet.setFormSize(10f);

                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);

                // X axis labels: S1, S2, ...
                ArrayList<String> labels = new ArrayList<>();
                labels.add("");  // padding for index 0
                for (int i = 0; i < entries.size(); i++) {
                    labels.add("S" + (i + 1));
                }
                lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

                // Animate — spec: animateXY(1000, 1000)
                lineChart.animateXY(1000, 1000);
                lineChart.invalidate();
            } else {
                lineChart.clear();
            }
        });

        // ── Weaknesses — UNCHANGED ──
        viewModel.getWeaknessesData().observe(getViewLifecycleOwner(), weaknesses -> {
            if (weaknesses != null) {
                adapter.setWeaknesses(weaknesses);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
