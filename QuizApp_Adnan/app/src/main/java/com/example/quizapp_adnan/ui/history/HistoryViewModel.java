package com.example.quizapp_adnan.ui.history;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizapp_adnan.data.model.Session;
import com.example.quizapp_adnan.data.repository.SessionRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryViewModel extends ViewModel {
    private final SessionRepository sessionRepository;
    private final String userId;

    private final MutableLiveData<List<Session>> sessions = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public HistoryViewModel() {
        sessionRepository = new SessionRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        loadSessions();
    }

    public LiveData<List<Session>> getSessions() { return sessions; }
    public LiveData<String> getError() { return error; }

    public void loadSessions() {
        if (userId.isEmpty()) return;

        sessionRepository.getUserSessions(userId).enqueue(new retrofit2.Callback<java.util.List<Session>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<Session>> call, retrofit2.Response<java.util.List<Session>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sessions.setValue(response.body());
                } else {
                    error.setValue("Erreur API : " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<Session>> call, Throwable t) {
                error.setValue("Échec réseau : " + t.getMessage());
            }
        });
    }
}
