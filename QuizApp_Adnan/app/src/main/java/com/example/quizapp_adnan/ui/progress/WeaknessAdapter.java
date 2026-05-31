package com.example.quizapp_adnan.ui.progress;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_adnan.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class WeaknessAdapter extends RecyclerView.Adapter<WeaknessAdapter.WeaknessViewHolder> {

    private List<ProgressViewModel.WeaknessItem> weaknesses = new ArrayList<>();
    private final OnMiniQuizClickListener listener;

    public interface OnMiniQuizClickListener {
        void onMiniQuizClick(String category);
    }

    public WeaknessAdapter(OnMiniQuizClickListener listener) {
        this.listener = listener;
    }

    public void setWeaknesses(List<ProgressViewModel.WeaknessItem> weaknesses) {
        this.weaknesses = weaknesses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WeaknessViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weakness, parent, false);
        return new WeaknessViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeaknessViewHolder holder, int position) {
        ProgressViewModel.WeaknessItem item = weaknesses.get(position);
        holder.tvCategoryName.setText("⚠️ " + item.category);
        holder.tvFailCount.setText("Raté " + item.failsCount + " fois (sur " + item.totalSessions + " sessions)");
        
        holder.btnMiniQuiz.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMiniQuizClick(item.category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return weaknesses.size();
    }

    static class WeaknessViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName;
        TextView tvFailCount;
        MaterialButton btnMiniQuiz;

        public WeaknessViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvFailCount = itemView.findViewById(R.id.tvFailCount);
            btnMiniQuiz = itemView.findViewById(R.id.btnMiniQuiz);
        }
    }
}
