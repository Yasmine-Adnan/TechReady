package com.example.quizapp_adnan.data.repository;

import com.example.quizapp_adnan.data.model.UserProfile;
import com.example.quizapp_adnan.data.remote.FirestoreDataSource;
import com.example.quizapp_adnan.data.remote.RetrofitClient;
import com.example.quizapp_adnan.data.remote.TechReadyApiService;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import retrofit2.Call;

public class ProfilingRepository {
    private final FirestoreDataSource dataSource;
    private final TechReadyApiService apiService;

    public ProfilingRepository() {
        dataSource = FirestoreDataSource.getInstance();
        apiService = RetrofitClient.getApiService();
    }

    // Récupère toutes les questions de profilage (Reste sur Firebase car pas d'API REST pour ça)
    public Task<QuerySnapshot> getProfilingQuestions() {
        return dataSource.getDb().collection("questions_profiling").orderBy("order").get();
    }

    // Sauvegarde le profil utilisateur généré (Migré vers Retrofit)
    public Call<Void> saveUserProfile(UserProfile profile) {
        return apiService.createProfile(profile);
    }

    // Vérifie et récupère le profil de l'utilisateur (Migré vers Retrofit)
    public Call<UserProfile> getUserProfile(String userId) {
        return apiService.getLastProfile(userId);
    }

    public Call<java.util.List<com.example.quizapp_adnan.data.model.SkillDTO>> getSkills(String userId) {
        return apiService.getSkills(userId);
    }
}
