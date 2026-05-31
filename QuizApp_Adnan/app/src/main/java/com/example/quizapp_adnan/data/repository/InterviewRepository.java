package com.example.quizapp_adnan.data.repository;

import com.example.quizapp_adnan.data.model.InterviewQuestion;
import com.example.quizapp_adnan.data.remote.RetrofitClient;
import com.example.quizapp_adnan.data.remote.TechReadyApiService;

import java.util.List;

import retrofit2.Call;

public class InterviewRepository {
    private final TechReadyApiService apiService;

    public InterviewRepository() {
        apiService = RetrofitClient.getApiService();
    }

    /**
     * Récupère les questions d'entretien via l'API REST.
     * Le backend gère automatiquement le fallback si moins de 5 questions sont trouvées.
     */
    public Call<List<InterviewQuestion>> getQuestions(String filiere, String specialite, String niveau) {
        return apiService.getQuestions(filiere, specialite, niveau, null, null);
    }

    /**
     * Récupère uniquement les questions compatibles avec le mode vocal (isVocalAccessible=true).
     */
    public Call<List<InterviewQuestion>> getVocalQuestions(String filiere, String specialite, String niveau) {
        return apiService.getQuestions(filiere, specialite, niveau, true, null);
    }

    /**
     * Récupère les questions filtrées par entreprise et/ou accessibilité vocale.
     */
    public Call<List<InterviewQuestion>> getQuestionsFiltered(String filiere, String specialite, String niveau,
                                                              Boolean isVocalAccessible, String entrepriseTag) {
        return apiService.getQuestions(filiere, specialite, niveau, isVocalAccessible, entrepriseTag);
    }
}
