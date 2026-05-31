package com.example.quizapp_adnan.ui.interview;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import androidx.annotation.NonNull;

import com.example.quizapp_adnan.data.model.InterviewQuestion;
import com.example.quizapp_adnan.data.model.UserProfile;
import com.example.quizapp_adnan.data.model.VocalSessionRequest;
import com.example.quizapp_adnan.data.model.VocalSessionResponse;
import com.example.quizapp_adnan.data.remote.RetrofitClient;
import com.example.quizapp_adnan.data.repository.InterviewRepository;
import com.example.quizapp_adnan.data.repository.ProfilingRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VocalModeViewModel extends AndroidViewModel {

    private final ProfilingRepository profilingRepository;
    private final InterviewRepository interviewRepository;
    private final String userId;
    private UserProfile currentProfile;

    private final List<InterviewQuestion> questionList = new ArrayList<>();
    private int currentIndex = 0;

    private final MutableLiveData<InterviewQuestion> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Integer> questionProgress = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFinished = new MutableLiveData<>();
    
    private final MutableLiveData<VocalSessionResponse> aiFeedback = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingAi = new MutableLiveData<>(false);

    public VocalModeViewModel(@NonNull Application application) {
        super(application);
        profilingRepository = new ProfilingRepository();
        interviewRepository = new InterviewRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        loadProfileAndQuestions();
    }

    public LiveData<InterviewQuestion> getCurrentQuestion() { return currentQuestion; }
    public LiveData<Integer> getQuestionProgress() { return questionProgress; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getIsFinished() { return isFinished; }
    public LiveData<VocalSessionResponse> getAiFeedback() { return aiFeedback; }
    public LiveData<Boolean> getIsLoadingAi() { return isLoadingAi; }

    private void loadProfileAndQuestions() {
        if (userId.isEmpty()) {
            error.setValue("Utilisateur non connecté");
            return;
        }

        profilingRepository.getUserProfile(userId).enqueue(new retrofit2.Callback<UserProfile>() {
            @Override
            public void onResponse(retrofit2.Call<UserProfile> call, retrofit2.Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    fetchQuestions();
                } else {
                    error.setValue("Profil introuvable");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<UserProfile> call, Throwable t) {
                error.setValue("Erreur de profil: " + t.getMessage());
            }
        });
    }

    private void fetchQuestions() {
        if (currentProfile == null) return;

        interviewRepository.getVocalQuestions(currentProfile.getFiliere(), currentProfile.getSpecialite(), currentProfile.getNiveau())
                .enqueue(new retrofit2.Callback<List<InterviewQuestion>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<InterviewQuestion>> call, retrofit2.Response<List<InterviewQuestion>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            // Des questions vocales existent — on les utilise directement
                            setupQuestions(response.body());
                        } else {
                            // Aucune question vocale trouvée : fallback sur toutes les questions du profil
                            android.util.Log.w("VocalModeVM", "Aucune question isVocalAccessible=true — fallback sur getQuestions()");
                            interviewRepository.getQuestions(currentProfile.getFiliere(), currentProfile.getSpecialite(), currentProfile.getNiveau())
                                    .enqueue(new retrofit2.Callback<List<InterviewQuestion>>() {
                                        @Override
                                        public void onResponse(retrofit2.Call<List<InterviewQuestion>> call2, retrofit2.Response<List<InterviewQuestion>> response2) {
                                            if (response2.isSuccessful() && response2.body() != null && !response2.body().isEmpty()) {
                                                setupQuestions(response2.body());
                                            } else {
                                                error.setValue("Aucune question disponible pour ce profil.");
                                            }
                                        }

                                        @Override
                                        public void onFailure(retrofit2.Call<List<InterviewQuestion>> call2, Throwable t) {
                                            error.setValue("Échec réseau (fallback) : " + t.getMessage());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<InterviewQuestion>> call, Throwable t) {
                        error.setValue("Échec réseau : " + t.getMessage());
                    }
                });
    }

    private void setupQuestions(List<InterviewQuestion> allMatched) {
        List<InterviewQuestion> filtered = new ArrayList<>();
        // In vocal mode, we prefer questions that are vocal accessible. 
        // If none are specifically tagged, we just take random ones.
        for(InterviewQuestion q : allMatched) {
            if (Boolean.TRUE.equals(q.getIsVocalAccessible())) {
                filtered.add(q);
            }
        }
        
        if (filtered.isEmpty()) {
            filtered.addAll(allMatched);
        }

        Collections.shuffle(filtered);
        // Requirement: 3 questions
        int limit = Math.min(3, filtered.size());
        for (int i = 0; i < limit; i++) {
            questionList.add(filtered.get(i));
        }

        if (questionList.isEmpty()) {
            error.setValue("Aucune question disponible.");
        } else {
            showCurrentQuestion();
        }
    }

    private void showCurrentQuestion() {
        if (currentIndex < questionList.size()) {
            currentQuestion.setValue(questionList.get(currentIndex));
            questionProgress.setValue(currentIndex + 1);
            aiFeedback.setValue(null); // Reset feedback for next question
        } else {
            // Verification Badge Orateur
            checkOrateurBadge();
            
            // Fin de la session vocale
            isFinished.setValue(true);
            // On pourrait ici sauvegarder une session finale de type VOCAL_SIMULATION via sessionRepository
            // (La sauvegarde détaillée peut aussi être déléguée à ResultActivity ou complétée ici plus tard)
        }
    }

    private void checkOrateurBadge() {
        com.example.quizapp_adnan.data.repository.SessionRepository sessionRepo = new com.example.quizapp_adnan.data.repository.SessionRepository();
        sessionRepo.getUserSessions(userId).enqueue(new retrofit2.Callback<List<com.example.quizapp_adnan.data.model.Session>>() {
            @Override
            public void onResponse(retrofit2.Call<List<com.example.quizapp_adnan.data.model.Session>> call, retrofit2.Response<List<com.example.quizapp_adnan.data.model.Session>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int vocalCount = 0;
                    for (com.example.quizapp_adnan.data.model.Session s : response.body()) {
                        if (s.getTypeMode() != null && s.getTypeMode().contains("VOCAL")) {
                            vocalCount++;
                        }
                    }
                    // +1 car la session actuelle vient de se terminer
                    if (vocalCount + 1 >= 3) {
                        com.example.quizapp_adnan.utils.BadgeManager.checkAndAwardBadge(getApplication(), userId, "orateur", "Orateur");
                    }
                }
            }
            @Override
            public void onFailure(retrofit2.Call<List<com.example.quizapp_adnan.data.model.Session>> call, Throwable t) {}
        });
    }

    public void validateAnswer(String transcribedText) {
        if (currentIndex >= questionList.size()) return;

        InterviewQuestion currentQ = questionList.get(currentIndex);
        isLoadingAi.setValue(true);

        VocalSessionRequest request = new VocalSessionRequest(currentQ.getId(), userId, transcribedText);
        
        RetrofitClient.getApiService().evaluateVocalSession(request).enqueue(new retrofit2.Callback<VocalSessionResponse>() {
            @Override
            public void onResponse(retrofit2.Call<VocalSessionResponse> call, retrofit2.Response<VocalSessionResponse> response) {
                isLoadingAi.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    aiFeedback.setValue(response.body());
                } else {
                    error.setValue("L'évaluation IA a échoué.");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<VocalSessionResponse> call, Throwable t) {
                isLoadingAi.setValue(false);
                error.setValue("Erreur réseau: " + t.getMessage());
            }
        });
    }

    public void nextQuestion() {
        currentIndex++;
        showCurrentQuestion();
    }
}
