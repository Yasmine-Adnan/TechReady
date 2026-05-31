package com.example.quizapp_adnan.ui.profiling;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import android.app.Application;
import androidx.annotation.NonNull;

import com.example.quizapp_adnan.data.model.ProfilingQuestion;
import com.example.quizapp_adnan.data.model.UserProfile;
import com.example.quizapp_adnan.data.repository.ProfilingRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ProfilingViewModel extends AndroidViewModel {
    private final ProfilingRepository repository;
    private final FirebaseAuth mAuth;

    private final List<ProfilingQuestion> allQuestions = new ArrayList<>();
    
    // Pile pour mémoriser le chemin parcouru et permettre le retour en arrière
    private final Stack<ProfilingQuestion> questionHistory = new Stack<>();
    // Dictionnaire des réponses de l'utilisateur (questionId -> Réponse)
    private final Map<String, Object> answers = new HashMap<>();

    private final MutableLiveData<ProfilingQuestion> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFinished = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ProfilingViewModel(@NonNull Application application) {
        super(application);
        repository = new ProfilingRepository();
        mAuth = FirebaseAuth.getInstance();
        loadQuestions();
    }

    public LiveData<ProfilingQuestion> getCurrentQuestion() { return currentQuestion; }
    public LiveData<Boolean> getIsFinished() { return isFinished; }
    public LiveData<String> getError() { return error; }

    private void loadQuestions() {
        repository.getProfilingQuestions().addOnSuccessListener(querySnapshot -> {
            allQuestions.clear();
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                allQuestions.add(doc.toObject(ProfilingQuestion.class));
            }
            if (!allQuestions.isEmpty()) {
                startProfiling();
            } else {
                error.setValue("Aucune question de profilage n'a été trouvée.");
            }
        }).addOnFailureListener(e -> error.setValue("Erreur de chargement: " + e.getMessage()));
    }

    private void startProfiling() {
        // Recherche de la question racine (qui n'a pas de parent)
        for (ProfilingQuestion q : allQuestions) {
            if (q.getParentQuestionId() == null) {
                questionHistory.push(q);
                currentQuestion.setValue(q);
                return;
            }
        }
    }

    public void submitAnswer(Object answer) {
        ProfilingQuestion current = currentQuestion.getValue();
        if (current == null) return;

        // Enregistrer la réponse
        answers.put(current.getId(), answer);

        // Trouver la question suivante en fonction de la réponse
        ProfilingQuestion next = getNextQuestion(current, answer);
        if (next != null) {
            questionHistory.push(next);
            currentQuestion.setValue(next);
        } else {
            // S'il n'y a plus de questions enfants, on a atteint la fin de la branche
            saveProfileAndFinish();
        }
    }

    public boolean goBack() {
        if (questionHistory.size() > 1) {
            // Retire la question courante
            questionHistory.pop();
            // Retourne à la question précédente sans la retirer de la pile
            ProfilingQuestion previous = questionHistory.peek();
            currentQuestion.setValue(previous);
            return true;
        }
        return false; // Impossible de reculer (on est déjà sur la première question)
    }

    private ProfilingQuestion getNextQuestion(ProfilingQuestion current, Object answer) {
        String answerString = null;
        if (answer instanceof String) {
            answerString = (String) answer;
        }

        // Logique de l'arbre adaptatif : on cherche la question dont le parent est 'current'
        // et qui correspond à la réponse donnée (ou qui s'applique à toutes les réponses si parentAnswer est null)
        List<ProfilingQuestion> candidates = new ArrayList<>();
        for (ProfilingQuestion q : allQuestions) {
            if (current.getId().equals(q.getParentQuestionId())) {
                if (q.getParentAnswer() == null || q.getParentAnswer().equals(answerString)) {
                    candidates.add(q);
                }
            }
        }

        if (candidates.isEmpty()) return null;
        
        // On trie par 'order' au cas où il y en aurait plusieurs (ex: séquentiel)
        candidates.sort((q1, q2) -> Integer.compare(q1.getOrder(), q2.getOrder()));
        return candidates.get(0);
    }

    private void saveProfileAndFinish() {
        if (mAuth.getCurrentUser() == null) {
            error.setValue("Utilisateur non connecté !");
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();
        String firebaseAuthName = mAuth.getCurrentUser().getDisplayName();

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener(doc -> {
                String finalName = firebaseAuthName;
                if ((finalName == null || finalName.trim().isEmpty()) && doc.exists()) {
                    finalName = doc.getString("displayName");
                }
                if (finalName == null || finalName.trim().isEmpty()) {
                    finalName = "Anonyme";
                }

                UserProfile profile = new UserProfile();
                profile.setUserId(uid);
                profile.setDisplayName(finalName);
                profile.setCreatedAt(new Date());

                // Extraction des valeurs selon les "fieldKey" (clés dynamiques associées aux questions)
                for (ProfilingQuestion q : questionHistory) {
                    Object ans = answers.get(q.getId());
                    if (ans != null && q.getFieldKey() != null) {
                        switch (q.getFieldKey()) {
                            case "filiere":
                                profile.setFiliere((String) ans);
                                break;
                            case "specialite":
                                profile.setSpecialite((String) ans);
                                break;
                            case "niveau":
                                profile.setNiveau((String) ans);
                                break;
                            case "typeContrat":
                                profile.setTypeContrat((String) ans);
                                break;
                            case "technos":
                                try {
                                    profile.setTechnos((List<String>) ans);
                                } catch (ClassCastException ignored) {}
                                break;
                            case "objectif":
                                String obj = (String) ans;
                                profile.setObjectif(obj);
                                if (obj != null && obj.contains("Panic")) {
                                    profile.setPanicMode(true);
                                } else {
                                    profile.setPanicMode(false);
                                }
                                break;
                        }
                    }
                }

                repository.saveUserProfile(profile).enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Update Firestore so the UI sees it immediately
                            Map<String, Object> lastProfileMap = new HashMap<>();
                            lastProfileMap.put("filiere", profile.getFiliere());
                            lastProfileMap.put("specialite", profile.getSpecialite());
                            lastProfileMap.put("niveau", profile.getNiveau());
                            
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(uid)
                                .update("lastProfile", lastProfileMap)
                                .addOnCompleteListener(task -> {
                                    // Mettre à jour SharedPreferences pour le Leaderboard
                                    android.content.SharedPreferences prefs = getApplication().getSharedPreferences("TechReadyPrefs", android.content.Context.MODE_PRIVATE);
                                    prefs.edit().putString("filiere", profile.getFiliere()).apply();
                                    
                                    isFinished.setValue(true);
                                });
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Erreur inconnue";
                                android.util.Log.e("PROFILING_ERROR", "Body refusé : " + errorBody);
                                error.setValue("Validation serveur échouée : " + errorBody);
                            } catch (Exception e) {
                                error.setValue("Erreur API de sauvegarde : " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                        error.setValue("Erreur réseau (Sauvegarde) : " + t.getMessage());
                    }
                });
            })
            .addOnFailureListener(e -> error.setValue("Erreur lors de la récupération du profil : " + e.getMessage()));
    }

    // Permet à la vue de pré-cocher les options si l'utilisateur fait un retour arrière
    public Object getPreviousAnswerForCurrentQuestion() {
        ProfilingQuestion current = currentQuestion.getValue();
        if (current != null) {
            return answers.get(current.getId());
        }
        return null;
    }
}
