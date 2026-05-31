package com.example.quizapp_adnan.ui.interview;

import android.os.CountDownTimer;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import androidx.annotation.NonNull;

import com.example.quizapp_adnan.data.model.InterviewQuestion;
import com.example.quizapp_adnan.data.model.Session;
import com.example.quizapp_adnan.data.model.UserProfile;
import com.example.quizapp_adnan.data.repository.InterviewRepository;
import com.example.quizapp_adnan.data.repository.ProfilingRepository;
import com.example.quizapp_adnan.data.repository.SessionRepository;
import com.example.quizapp_adnan.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterviewViewModel extends AndroidViewModel {
    private final InterviewRepository interviewRepository;
    private final ProfilingRepository profilingRepository;
    private final SessionRepository sessionRepository;
    private final String userId;

    private UserProfile currentProfile;
    private final List<InterviewQuestion> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;

    private CountDownTimer timer;
    private final MutableLiveData<Integer> timeLeft = new MutableLiveData<>();
    
    private final MutableLiveData<InterviewQuestion> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Integer> questionProgress = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFinished = new MutableLiveData<>();
    
    // Pour l'overlay d'explication
    private final MutableLiveData<ExplanationData> explanationData = new MutableLiveData<>();
    
    // Garder trace des réponses pour la Session (historique)
    private final List<Map<String, Object>> questionsAnswered = new ArrayList<>();
    private long sessionStartTime;

    public InterviewViewModel(@NonNull Application application) {
        super(application);
        interviewRepository = new InterviewRepository();
        profilingRepository = new ProfilingRepository();
        sessionRepository = new SessionRepository();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        
        loadProfileAndQuestions();
    }

    public LiveData<InterviewQuestion> getCurrentQuestion() { return currentQuestion; }
    public LiveData<Integer> getTimeLeft() { return timeLeft; }
    public LiveData<Integer> getQuestionProgress() { return questionProgress; }
    public LiveData<ExplanationData> getExplanationData() { return explanationData; }
    public LiveData<Boolean> getIsFinished() { return isFinished; }
    public LiveData<String> getError() { return error; }

    private void loadProfileAndQuestions() {
        if (userId.isEmpty()) {
            error.setValue("Utilisateur non connecté");
            return;
        }

        // On récupère le profil via l'API pour filtrer les questions
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
                .enqueue(new retrofit2.Callback<java.util.List<InterviewQuestion>>() {
                    @Override
                    public void onResponse(retrofit2.Call<java.util.List<InterviewQuestion>> call, retrofit2.Response<java.util.List<InterviewQuestion>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            java.util.List<InterviewQuestion> fetched = response.body();
                            setupQuestions(fetched);
                        } else {
                            error.setValue("Erreur API : " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<java.util.List<InterviewQuestion>> call, Throwable t) {
                        error.setValue("Échec réseau : " + t.getMessage());
                    }
                });
    }

    private void setupQuestions(List<InterviewQuestion> allMatched) {
        List<InterviewQuestion> filtered = new ArrayList<>();
        for(InterviewQuestion q : allMatched) {
            if (q.getTags() != null && q.getTags().contains("mode_quick")) {
                filtered.add(q);
            }
        }
        
        // Fallback si pas assez de questions taggées
        if (filtered.isEmpty()) {
            filtered.addAll(allMatched);
        }

        Collections.shuffle(filtered);
        int limit = Math.min(5, filtered.size());
        for (int i = 0; i < limit; i++) {
            questionList.add(filtered.get(i));
        }

        if (questionList.isEmpty()) {
            error.setValue("Aucune question disponible pour ce profil dans la base.");
        } else {
            sessionStartTime = System.currentTimeMillis();
            showCurrentQuestion();
        }
    }

    private void showCurrentQuestion() {
        if (currentIndex < questionList.size()) {
            currentQuestion.setValue(questionList.get(currentIndex));
            questionProgress.setValue(currentIndex + 1);
            startTimer();
        } else {
            finishInterview();
        }
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        // 3 minutes = 180 000 ms
        timer = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft.setValue((int) (millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                // Temps écoulé -> on soumet automatiquement une mauvaise réponse (index invalide)
                submitAnswer(-1);
            }
        }.start();
    }

    public void submitAnswer(int selectedIndex) {
        if (timer != null) timer.cancel();

        InterviewQuestion q = questionList.get(currentIndex);
        boolean isCorrect = (selectedIndex == q.getCorrectIndex());
        
        if (isCorrect) {
            score++;
        }

        // On sauvegarde le détail pour l'historique
        Map<String, Object> ansMap = new HashMap<>();
        ansMap.put("questionId", q.getId());
        ansMap.put("selectedIndex", selectedIndex);
        ansMap.put("isCorrect", isCorrect);
        questionsAnswered.add(ansMap);

        // On déclenche l'overlay d'explication
        explanationData.setValue(new ExplanationData(isCorrect, q.getCorrectIndex(), q.getExplanation()));
    }

    public void nextQuestion() {
        explanationData.setValue(null); // On masque l'overlay
        currentIndex++;
        showCurrentQuestion();
    }

    private void finishInterview() {
        // Construction de l'objet Session
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
        session.setTypeMode("QUICK_QUIZ");

        // 1. Sauvegarde de la session (Historique) via l'API REST
        sessionRepository.saveSession(session).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    // 2. Mise à jour des statistiques globales de l'utilisateur (Reste sur Firebase pour le User)
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
                                userRepo.saveUser(u).addOnCompleteListener(t -> {
                                    if (u.getTotalSessions() == 1) {
                                        com.example.quizapp_adnan.utils.BadgeManager.checkAndAwardBadge(getApplication(), userId, "premier_pas", "Premier Pas");
                                    }
                                    checkEnFeuBadge();
                                    isFinished.setValue(true);
                                });
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
        if (timer != null) timer.cancel();
    }

    private void checkEnFeuBadge() {
        sessionRepository.getUserSessions(userId).enqueue(new retrofit2.Callback<java.util.List<Session>>() {
            @Override
            public void onResponse(retrofit2.Call<java.util.List<Session>> call, retrofit2.Response<java.util.List<Session>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int todayCount = 0;
                    java.util.Calendar today = java.util.Calendar.getInstance();
                    for (Session s : response.body()) {
                        if (s.getDate() != null) {
                            java.util.Calendar sessionDate = java.util.Calendar.getInstance();
                            sessionDate.setTime(s.getDate());
                            if (sessionDate.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                                sessionDate.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)) {
                                todayCount++;
                            }
                        }
                    }
                    if (todayCount >= 3) {
                        com.example.quizapp_adnan.utils.BadgeManager.checkAndAwardBadge(getApplication(), userId, "en_feu", "En feu");
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<java.util.List<Session>> call, Throwable t) {
            }
        });
    }

    // Structure englobant les données de l'overlay (bon/mauvais, réponse et explication)
    public static class ExplanationData {
        public final boolean isCorrect;
        public final int correctIndex;
        public final String explanation;

        public ExplanationData(boolean isCorrect, int correctIndex, String explanation) {
            this.isCorrect = isCorrect;
            this.correctIndex = correctIndex;
            this.explanation = explanation;
        }
    }
}
