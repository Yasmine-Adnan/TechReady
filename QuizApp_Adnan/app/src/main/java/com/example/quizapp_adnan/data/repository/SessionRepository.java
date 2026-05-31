package com.example.quizapp_adnan.data.repository;

import com.example.quizapp_adnan.data.model.Session;
import com.example.quizapp_adnan.data.remote.RetrofitClient;
import com.example.quizapp_adnan.data.remote.TechReadyApiService;

import java.util.List;
import retrofit2.Call;

public class SessionRepository {
    private final TechReadyApiService apiService;

    public SessionRepository() {
        apiService = RetrofitClient.getApiService();
    }

    public Call<Void> saveSession(Session session) {
        return apiService.createSession(session);
    }

    public Call<List<Session>> getUserSessions(String userId) {
        return apiService.getUserSessions(userId);
    }
}
