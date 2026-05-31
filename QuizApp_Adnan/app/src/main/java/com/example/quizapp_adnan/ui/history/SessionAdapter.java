package com.example.quizapp_adnan.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.data.model.Session;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {
    private List<Session> sessions;

    public SessionAdapter(List<Session> sessions) {
        this.sessions = sessions;
    }

    public void setSessions(List<Session> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session session = sessions.get(position);
        holder.bind(session);
    }

    @Override
    public int getItemCount() {
        return sessions != null ? sessions.size() : 0;
    }

    static class SessionViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvScore, tvTime;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        public void bind(Session session) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy - HH:mm", Locale.getDefault());
            String dateStr = session.getDate() != null ? sdf.format(session.getDate()) : "Date inconnue";
            tvDate.setText(dateStr);

            int percentage = (int) session.getPercentage();
            tvScore.setText(session.getScore() + "/" + session.getTotal() + " (" + percentage + "%)");

            int minutes = session.getTimeTakenSeconds() / 60;
            int seconds = session.getTimeTakenSeconds() % 60;
            tvTime.setText(String.format(Locale.getDefault(), "Temps : %02d:%02d", minutes, seconds));

            if (percentage >= 80) {
                tvScore.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            } else if (percentage >= 50) {
                tvScore.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_orange_dark));
            } else {
                tvScore.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
            }
        }
    }
}
