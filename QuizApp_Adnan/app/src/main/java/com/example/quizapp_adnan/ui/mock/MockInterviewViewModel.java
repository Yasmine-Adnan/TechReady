package com.example.quizapp_adnan.ui.mock;

import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizapp_adnan.data.model.InterviewQuestion;
import com.example.quizapp_adnan.data.model.Session;
import com.example.quizapp_adnan.data.model.UserProfile;
import com.example.quizapp_adnan.data.repository.InterviewRepository;
import com.example.quizapp_adnan.data.repository.ProfilingRepository;
import com.example.quizapp_adnan.data.repository.SessionRepository;
import com.example.quizapp_adnan.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockInterviewViewModel extends ViewModel {
    private final InterviewRepository interviewRepository;
    private final ProfilingRepository profilingRepository;
    private final SessionRepository sessionRepository;
    private final String userId;

    private UserProfile currentProfile;
    private final List<InterviewQuestion> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;

    private CountDownTimer globalTimer;
    private final MutableLiveData<Integer> timeLeft = new MutableLiveData<>();
    private final int TOTAL_TIME_SECONDS = 300; // 5 minutes
    
    private final MutableLiveData<InterviewQuestion> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Integer> questionProgress = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFinished = new MutableLiveData<>();
    
    private final List<Map<String, Object>> questionsAnswered = new ArrayList<>();
    private long sessionStartTime;

    public MockInterviewViewModel() {
        interviewRepository = new InterviewRepository();
        profilingRepository = new ProfilingRepository();
        sessionRepository = new SessionRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        
        loadProfileAndQuestions();
    }

    public LiveData<InterviewQuestion> getCurrentQuestion() { return currentQuestion; }
    public LiveData<Integer> getTimeLeft() { return timeLeft; }
    public LiveData<Integer> getQuestionProgress() { return questionProgress; }
    public LiveData<Boolean> getIsFinished() { return isFinished; }
    public LiveData<String> getError() { return error; }
    public int getTotalQuestions() { return questionList.size(); }

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
        List<InterviewQuestion> filtered = new ArrayList<>();
        for(InterviewQuestion q : allMatched) {
            if (q.getTags() != null && q.getTags().contains("mode_mock")) {
                filtered.add(q);
            }
        }
        
        if (filtered.isEmpty()) {
            filtered.addAll(allMatched);
        }

        Collections.shuffle(filtered);
        // On prend jusqu'à 10 questions pour l'entretien blanc
        int limit = Math.min(10, filtered.size());
        for (int i = 0; i < limit; i++) {
            questionList.add(filtered.get(i));
        }

        if (questionList.isEmpty()) {
            error.setValue("Aucune question disponible pour ce profil.");
        } else {
            sessionStartTime = System.currentTimeMillis();
            startGlobalTimer();
            showCurrentQuestion();
        }
    }

    private void showCurrentQuestion() {
        if (currentIndex < questionList.size()) {
            currentQuestion.setValue(questionList.get(currentIndex));
            questionProgress.setValue(currentIndex + 1);
        } else {
            finishInterview();
        }
    }

    private void startGlobalTimer() {
        if (globalTimer != null) globalTimer.cancel();
        
        globalTimer = new CountDownTimer(TOTAL_TIME_SECONDS * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.setValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // Temps global écoulé
                error.setValue("Le temps imparti est écoulé !");
                finishInterview();
            }
        }.start();
    }

    public void submitAnswer(int selectedIndex) {
        InterviewQuestion q = questionList.get(currentIndex);
        boolean isCorrect = (selectedIndex == q.getCorrectIndex());
        
        if (isCorrect) {
            score++;
        }

        Map<String, Object> ansMap = new HashMap<>();
        ansMap.put("questionId", q.getId());
        ansMap.put("selectedIndex", selectedIndex);
        ansMap.put("isCorrect", isCorrect);
        questionsAnswered.add(ansMap);

        // Pas de feedback immédiat, on passe à la suite directement
        currentIndex++;
        showCurrentQuestion();
    }

    private void finishInterview() {
        if (globalTimer != null) globalTimer.cancel();
        
        // S'il n'a répondu à rien, on ferme juste
        if (questionsAnswered.isEmpty()) {
            isFinished.setValue(true);
            return;
        }

        Session session = new Session();
        session.setUserId(userId);
        session.setProfileId(userId); 
        session.setScore(score);
        session.setTotal(questionList.size());
        
        int percentage = (int) (((double) score / questionList.size()) * 100);
        session.setPercentage(percentage);
        session.setTimeTakenSeconds((int) ((System.currentTimeMillis() - sessionStartTime) / 1000));
        session.setDate(new Date());
        session.setQuestionsAnswered(questionsAnswered);

        List<String> failedCategories = new ArrayList<>();
        for (Map<String, Object> ans : questionsAnswered) {
            Boolean isCorrect = (Boolean) ans.get("isCorrect");
            if (isCorrect != null && !isCorrect) {
                String qId = (String) ans.get("questionId");
                for (InterviewQuestion q : questionList) {
                    if (q.getId() != null && q.getId().equals(qId)) {
                        if (q.getTags() != null) {
                            for (String tag : q.getTags()) {
                                if (!tag.startsWith("mode_") && !failedCategories.contains(tag)) {
                                    failedCategories.add(tag);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        session.setCategoriesEchouees(failedCategories);
        session.setTypeMode("MOCK_INTERVIEW");

        sessionRepository.saveSession(session).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    UserRepository userRepo = new UserRepository();
                    userRepo.getUser(userId).addOnSuccessListener(doc -> {
                        if(doc.exists()) {
                            com.example.quizapp_adnan.data.model.User u = doc.toObject(com.example.quizapp_adnan.data.model.User.class);
                            if (u != null) {
                                u.setTotalSessions(u.getTotalSessions() + 1);
                                u.setLastScore(percentage);
                                if (percentage > u.getBestScore()) {
                                    u.setBestScore(percentage);
                                }
                                userRepo.saveUser(u).addOnCompleteListener(t -> isFinished.setValue(true));
                            } else {
                                isFinished.setValue(true);
                            }
                        } else {
                            isFinished.setValue(true);
                        }
                    });
                } else {
                    error.setValue("Erreur API Sauvegarde Session");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                error.setValue("Erreur réseau: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (globalTimer != null) globalTimer.cancel();
    }
}
