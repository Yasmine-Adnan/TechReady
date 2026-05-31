package com.example.quizapp_adnan.ui.leaderboard;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.data.model.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {

    private List<UserProfile> profiles = new ArrayList<>();
    private String currentUserId = "";

    public void setProfiles(List<UserProfile> profiles) {
        this.profiles = profiles;
        notifyDataSetChanged();
    }

    public void setCurrentUserId(String uid) {
        this.currentUserId = uid != null ? uid : "";
    }

    @NonNull
    @Override
    public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
        UserProfile profile = profiles.get(position);
        int rank = position + 1;

        // ── Rang ──
        holder.rankTextView.setText(String.valueOf(rank));
        if (rank == 1) {
            holder.rankTextView.setBackgroundResource(R.drawable.circle_background_gold);
        } else if (rank == 2) {
            holder.rankTextView.setBackgroundResource(R.drawable.circle_background_silver);
        } else if (rank == 3) {
            holder.rankTextView.setBackgroundResource(R.drawable.circle_background_bronze);
        } else {
            holder.rankTextView.setBackgroundResource(R.drawable.circle_background_primary);
        }

        // ── Nom ──
        String displayName = profile.getDisplayName();
        boolean isCurrentUser = profile.getUserId() != null
                && profile.getUserId().equals(currentUserId);

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = isCurrentUser ? "Anonyme (Vous)" : "Anonyme";
        } else if (isCurrentUser) {
            displayName = displayName + " (Vous)";
        }
        holder.nameTextView.setText(displayName);

        // ── Spécialité + niveau ──
        String spec = profile.getSpecialite();
        int level = profile.getTotalPoints() > 0
                ? Math.max(1, (int)(profile.getTotalPoints() / 100)) : 1;
        String subtitle = (spec != null && !spec.isEmpty() ? spec : "—") + " • Niveau " + level;
        holder.specialiteTextView.setText(subtitle);

        // ── Points ──
        long pts = profile.getTotalPoints();
        holder.pointsTextView.setText(formatPoints(pts) + " pts");

        // ── Highlight row si c'est l'utilisateur courant ──
        if (isCurrentUser) {
            holder.rowContainer.setBackgroundResource(R.drawable.bg_leaderboard_me);
        } else {
            holder.rowContainer.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /** Formate 1200 → "1 200" pour la lisibilité */
    private String formatPoints(long pts) {
        if (pts >= 1000) {
            return String.format("%,d", pts).replace(",", "\u202F");
        }
        return String.valueOf(pts);
    }

    static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rowContainer;
        TextView rankTextView, nameTextView, specialiteTextView, pointsTextView;

        public LeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            rowContainer      = itemView.findViewById(R.id.rowContainer);
            rankTextView      = itemView.findViewById(R.id.rankTextView);
            nameTextView      = itemView.findViewById(R.id.nameTextView);
            specialiteTextView= itemView.findViewById(R.id.specialiteTextView);
            pointsTextView    = itemView.findViewById(R.id.pointsTextView);
        }
    }
}
