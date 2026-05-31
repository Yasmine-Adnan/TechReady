package com.example.quizapp_adnan.ui.survival;

import android.os.CountDownTimer;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import androidx.annotation.NonNull;

import com.example.quizapp_adnan.data.model.InterviewQuestion;
import com.example.quizapp_adnan.data.model.UserProfile;
import com.example.quizapp_adnan.data.repository.InterviewRepository;
import com.example.quizapp_adnan.data.repository.ProfilingRepository;
import com.example.quizapp_adnan.ui.interview.InterviewViewModel.ExplanationData;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SurvivalViewModel extends AndroidViewModel {
    private final InterviewRepository interviewRepository;
    private final ProfilingRepository profilingRepository;
    private final String userId;

    private UserProfile currentProfile;
    private final List<InterviewQuestion> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int survivedCount = 0;
    private int consecutiveCorrect = 0; // Pour le badge survivant
    
    // Mode Survie spécifiques
    private int lives = 3;
    private final MutableLiveData<Integer> livesLeft = new MutableLiveData<>(lives);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);

    private CountDownTimer timer;
    private final MutableLiveData<Integer> timeLeft = new MutableLiveData<>();
    
    private final MutableLiveData<InterviewQuestion> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isGameOver = new MutableLiveData<>(false);
    
    // Pour l'overlay d'explication
    private final MutableLiveData<ExplanationData> explanationData = new MutableLiveData<>();

    public SurvivalViewModel(@NonNull Application application) {
        super(application);
        interviewRepository = new InterviewRepository();
        profilingRepository = new ProfilingRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        
        loadProfileAndQuestions();
    }

    public LiveData<InterviewQuestion> getCurrentQuestion() { return currentQuestion; }
    public LiveData<Integer> getTimeLeft() { return timeLeft; }
    public LiveData<Integer> getLivesLeft() { return livesLeft; }
    public LiveData<Integer> getScore() { return score; }
    public LiveData<ExplanationData> getExplanationData() { return explanationData; }
    public LiveData<Boolean> getIsGameOver() { return isGameOver; }
    public LiveData<String> getError() { return error; }

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

        interviewRepository.getQuestions(currentProfile.getFiliere(), currentProfile.getSpecialite(), currentProfile.getNiveau())
                .enqueue(new retrofit2.Callback<List<InterviewQuestion>>() {
                    @Override
                    public void onResponse(retrofit2.Call<List<InterviewQuestion>> call, retrofit2.Response<List<InterviewQuestion>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<InterviewQuestion> fetched = response.body();
                            setupQuestions(fetched);
                        } else {
                            error.setValue("Erreur API : " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<List<InterviewQuestion>> call, Throwable t) {
                        error.setValue("Échec réseau : " + t.getMessage());
                    }
                });
    }

    private void setupQuestions(List<InterviewQuestion> allMatched) {
        if (allMatched == null || allMatched.isEmpty()) {
            error.setValue("Aucune question disponible pour ce profil.");
            return;
        }
        
        List<InterviewQuestion> filtered = new ArrayList<>();
        for(InterviewQuestion q : allMatched) {
            if (q.getTags() != null && q.getTags().contains("mode_survival")) {
                filtered.add(q);
            }
        }
        
        if (filtered.isEmpty()) {
            filtered.addAll(allMatched);
        }
        
        Collections.shuffle(filtered);
        questionList.addAll(filtered);
        showCurrentQuestion();
    }

    private void showCurrentQuestion() {
        if (lives <= 0) {
            triggerGameOver();
            return;
        }

        // Si on atteint la fin de la liste, on la mélange et on repart à zéro pour l'INFINI !
        if (currentIndex >= questionList.size()) {
            Collections.shuffle(questionList);
            currentIndex = 0;
        }

        currentQuestion.setValue(questionList.get(currentIndex));
        startTimer();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        // 10 secondes par question
        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.setValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // Temps écoulé = mauvaise réponse automatique
                submitAnswer(-1);
            }
        }.start();
    }

    public void submitAnswer(int selectedIndex) {
        if (timer != null) timer.cancel();

        InterviewQuestion q = questionList.get(currentIndex);
        boolean isCorrect = (selectedIndex == q.getCorrectIndex());
        
        if (isCorrect) {
            survivedCount++;
            consecutiveCorrect++;
            score.setValue(survivedCount);
            
            if (consecutiveCorrect == 10) {
                com.example.quizapp_adnan.utils.BadgeManager.checkAndAwardBadge(getApplication(), userId, "survivant", "Survivant");
            }
        } else {
            consecutiveCorrect = 0;
            lives--;
            livesLeft.setValue(lives);
        }

        explanationData.setValue(new ExplanationData(isCorrect, q.getCorrectIndex(), q.getExplanation()));
    }

    public void nextQuestion() {
        explanationData.setValue(null);
        currentIndex++;
        showCurrentQuestion();
    }

    private void triggerGameOver() {
        if (timer != null) timer.cancel();
        // Optionnel: enregistrer le high score en DB ici si besoin
        isGameOver.setValue(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (timer != null) timer.cancel();
    }
}
