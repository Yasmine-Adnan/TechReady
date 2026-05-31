package com.example.quizapp_adnan.ui.result;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizapp_adnan.data.model.Session;
import com.example.quizapp_adnan.data.model.UserProfile;
import com.example.quizapp_adnan.data.remote.RetrofitClient;
import com.example.quizapp_adnan.data.repository.ProfilingRepository;
import com.example.quizapp_adnan.data.repository.SessionRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class ResultViewModel extends ViewModel {
    private final SessionRepository sessionRepository;
    private final ProfilingRepository profilingRepository;
    private final String userId;

    private final MutableLiveData<Session> lastSession = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> aiFeedback = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingAi = new MutableLiveData<>(false);

    public ResultViewModel() {
        sessionRepository = new SessionRepository();
        profilingRepository = new ProfilingRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
    }

    public LiveData<Session> getLastSession() { return lastSession; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getAiFeedback() { return aiFeedback; }
    public LiveData<Boolean> getIsLoadingAi() { return isLoadingAi; }

    public void loadLastSession() {
        if (userId.isEmpty()) return;
        
        sessionRepository.getUserSessions(userId).enqueue(new retrofit2.Callback<java.util.List<Session>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<Session>> call, retrofit2.Response<java.util.List<Session>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    // Le backend renvoie les sessions triées par date DESC, la première est la plus récente
                    Session session = response.body().get(0);
                    lastSession.setValue(session);
                    fetchAiAnalysis(session);
                } else {
                    error.setValue("Aucune session trouvée.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<Session>> call, Throwable t) {
                error.setValue("Erreur réseau: " + t.getMessage());
            }
        });
    }

    private void fetchAiAnalysis(Session session) {
        isLoadingAi.setValue(true);
        profilingRepository.getUserProfile(userId).enqueue(new retrofit2.Callback<UserProfile>() {
            @Override
            public void onResponse(retrofit2.Call<UserProfile> call, retrofit2.Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();
                    requestGeminiFeedback(session, profile);
                } else {
                    isLoadingAi.setValue(false);
                    error.setValue("Impossible de charger le profil pour l'IA.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserProfile> call, Throwable t) {
                isLoadingAi.setValue(false);
                error.setValue("Erreur réseau profil.");
            }
        });
    }

    private void requestGeminiFeedback(Session session, UserProfile profile) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("score", session.getScore());
        payload.put("total", session.getTotal());
        payload.put("filiere", profile.getFiliere());
        payload.put("specialite", profile.getSpecialite());
        payload.put("niveau", profile.getNiveau());
        payload.put("questionsAnswered", session.getQuestionsAnswered());

        RetrofitClient.getApiService().analyzeSession(payload).enqueue(new retrofit2.Callback<Map<String, String>>() {
            @Override
            public void onResponse(retrofit2.Call<Map<String, String>> call, retrofit2.Response<Map<String, String>> response) {
                isLoadingAi.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    aiFeedback.setValue(response.body().get("feedback"));
                } else {
                    aiFeedback.setValue("L'IA Gemini est indisponible pour le moment.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Map<String, String>> call, Throwable t) {
                isLoadingAi.setValue(false);
                aiFeedback.setValue("Erreur de connexion à l'IA Gemini.");
            }
        });
    }
}
