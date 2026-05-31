package com.example.quizapp_adnan.ui.history;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_adnan.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private HistoryViewModel viewModel;
    private SessionAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private Button bBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        bBack = findViewById(R.id.bBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SessionAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        setupObservers();
        
        bBack.setOnClickListener(v -> finish()); // Retourne simplement à l'écran précédent
    }

    private void setupObservers() {
        viewModel.getSessions().observe(this, sessions -> {
            if (sessions != null && !sessions.isEmpty()) {
                adapter.setSessions(sessions);
                recyclerView.setVisibility(View.VISIBLE);
                tvEmptyMessage.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                tvEmptyMessage.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
