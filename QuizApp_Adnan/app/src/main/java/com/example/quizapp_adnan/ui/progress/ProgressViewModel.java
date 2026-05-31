package com.example.quizapp_adnan.ui.progress;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizapp_adnan.data.model.Session;
import com.example.quizapp_adnan.data.remote.RetrofitClient;
import com.example.quizapp_adnan.data.remote.TechReadyApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressViewModel extends ViewModel {

    private final MutableLiveData<List<Entry>> chartData = new MutableLiveData<>();
    private final MutableLiveData<List<WeaknessItem>> weaknessesData = new MutableLiveData<>();
    private final MutableLiveData<String> motivationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private TechReadyApiService apiService;

    public ProgressViewModel() {
        apiService = RetrofitClient.getApiService();
    }

    public LiveData<List<Entry>> getChartData() { return chartData; }
    public LiveData<List<WeaknessItem>> getWeaknessesData() { return weaknessesData; }
    public LiveData<String> getMotivationMessage() { return motivationMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getError() { return error; }

    public void fetchProgressData() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        isLoading.setValue(true);
        apiService.getUserSessions(userId).enqueue(new Callback<List<Session>>() {
            @Override
            public void onResponse(Call<List<Session>> call, Response<List<Session>> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    processSessions(response.body());
                } else if (response.code() == 404) {
                    // No sessions
                    chartData.setValue(new ArrayList<>());
                    weaknessesData.setValue(new ArrayList<>());
                } else {
                    error.setValue("Erreur lors du chargement de l'historique");
                }
            }

            @Override
            public void onFailure(Call<List<Session>> call, Throwable t) {
                isLoading.setValue(false);
                error.setValue("Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void processSessions(List<Session> sessions) {
        if (sessions.isEmpty()) {
            chartData.setValue(new ArrayList<>());
            weaknessesData.setValue(new ArrayList<>());
            return;
        }

        // Sort sessions by date
        Collections.sort(sessions, new Comparator<Session>() {
            @Override
            public int compare(Session s1, Session s2) {
                if (s1.getDate() == null || s2.getDate() == null) return 0;
                return s1.getDate().compareTo(s2.getDate());
            }
        });

        // 1. Process Chart Data
        Map<Integer, List<Double>> scoresByWeek = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        
        for (Session session : sessions) {
            if (session.getDate() == null) continue;
            cal.setTime(session.getDate());
            int weekOfYear = cal.get(Calendar.WEEK_OF_YEAR);
            
            if (!scoresByWeek.containsKey(weekOfYear)) {
                scoresByWeek.put(weekOfYear, new ArrayList<>());
            }
            scoresByWeek.get(weekOfYear).add(session.getPercentage());
        }

        List<Integer> sortedWeeks = new ArrayList<>(scoresByWeek.keySet());
        Collections.sort(sortedWeeks);

        List<Entry> entries = new ArrayList<>();
        int weekIndex = 1; // S1, S2, etc. (we can just map index to 1, 2, 3...)
        Double lastAverage = null;
        Double previousAverage = null;

        for (Integer week : sortedWeeks) {
            List<Double> weekScores = scoresByWeek.get(week);
            double sum = 0;
            for (Double s : weekScores) sum += s;
            double avg = sum / weekScores.size();
            
            entries.add(new Entry(weekIndex, (float) avg));
            weekIndex++;
            
            previousAverage = lastAverage;
            lastAverage = avg;
        }
        
        chartData.setValue(entries);

        if (previousAverage != null && lastAverage != null) {
            double diff = lastAverage - previousAverage;
            if (diff > 0) {
                motivationMessage.setValue(String.format("📈 +%.1f%% par rapport à la dernière session, continue !", diff));
            } else if (diff < 0) {
                motivationMessage.setValue(String.format("📉 %.1f%%. Ne te décourage pas !", diff));
            } else {
                motivationMessage.setValue("Tu maintiens ton niveau !");
            }
        }

        // 2. Process Weaknesses
        Map<String, Integer> categoryFails = new HashMap<>();
        int totalSessions = sessions.size();
        
        for (Session session : sessions) {
            if (session.getCategoriesEchouees() != null) {
                for (String cat : session.getCategoriesEchouees()) {
                    categoryFails.put(cat, categoryFails.getOrDefault(cat, 0) + 1);
                }
            }
        }

        List<WeaknessItem> weaknesses = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categoryFails.entrySet()) {
            // We consider it a weakness if failed more than once, or we just display all
            weaknesses.add(new WeaknessItem(entry.getKey(), entry.getValue(), totalSessions));
        }

        // Sort by most fails
        Collections.sort(weaknesses, (w1, w2) -> Integer.compare(w2.failsCount, w1.failsCount));
        weaknessesData.setValue(weaknesses);
    }

    public static class WeaknessItem {
        public String category;
        public int failsCount;
        public int totalSessions;

        public WeaknessItem(String category, int failsCount, int totalSessions) {
            this.category = category;
            this.failsCount = failsCount;
            this.totalSessions = totalSessions;
        }
    }
}
