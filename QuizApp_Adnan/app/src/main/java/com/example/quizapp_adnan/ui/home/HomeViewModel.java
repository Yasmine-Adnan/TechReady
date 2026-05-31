package com.example.quizapp_adnan.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizapp_adnan.data.model.User;
import com.example.quizapp_adnan.data.repository.ProfilingRepository;
import com.example.quizapp_adnan.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final ProfilingRepository profilingRepository;
    private final FirebaseAuth mAuth;

    private final MutableLiveData<User> currentUserData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasProfile = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<java.util.List<com.example.quizapp_adnan.data.model.SkillDTO>> skillsData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> panicMode = new MutableLiveData<>(false);

    public HomeViewModel() {
        userRepository = new UserRepository();
        profilingRepository = new ProfilingRepository();
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<User> getCurrentUserData() {
        return currentUserData;
    }

    public LiveData<Boolean> getHasProfile() {
        return hasProfile;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<java.util.List<com.example.quizapp_adnan.data.model.SkillDTO>> getSkillsData() {
        return skillsData;
    }

    public LiveData<Boolean> getPanicMode() {
        return panicMode;
    }

    public void loadUserData() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser != null) {
            String uid = fUser.getUid();
            
            // 1. Charger les données utilisateur (pour afficher les stats : sessions, score)
            userRepository.getUser(uid).addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    currentUserData.setValue(user);
                }
            }).addOnFailureListener(e -> {
                errorMessage.setValue("Erreur de chargement des statistiques");
            });

            // 2. Vérifier si l'utilisateur a déjà complété son profil via l'API REST
            profilingRepository.getUserProfile(uid).enqueue(new retrofit2.Callback<com.example.quizapp_adnan.data.model.UserProfile>() {
                @Override
                public void onResponse(retrofit2.Call<com.example.quizapp_adnan.data.model.UserProfile> call, retrofit2.Response<com.example.quizapp_adnan.data.model.UserProfile> response) {
                    hasProfile.setValue(response.isSuccessful() && response.body() != null);
                    // Pour l'étape 3 on gère le panicMode, simulons-le ici ou s'il existe dans UserProfile
                    // panicMode.setValue(response.body().getPanicMode()); // sera ajouté plus tard
                }

                @Override
                public void onFailure(retrofit2.Call<com.example.quizapp_adnan.data.model.UserProfile> call, Throwable t) {
                    errorMessage.setValue("Erreur réseau profil");
                }
            });
        }
    }

    public void logout() {
        mAuth.signOut();
    }

    public void fetchSkills() {
        FirebaseUser fUser = mAuth.getCurrentUser();
        if (fUser != null) {
            profilingRepository.getSkills(fUser.getUid()).enqueue(new retrofit2.Callback<java.util.List<com.example.quizapp_adnan.data.model.SkillDTO>>() {
                @Override
                public void onResponse(retrofit2.Call<java.util.List<com.example.quizapp_adnan.data.model.SkillDTO>> call, retrofit2.Response<java.util.List<com.example.quizapp_adnan.data.model.SkillDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        java.util.List<com.example.quizapp_adnan.data.model.SkillDTO> skills =
                                new java.util.ArrayList<>(response.body());

                        // Le RadarChart nécessite au moins 3 points pour dessiner un polygone.
                        // On complète avec des catégories standard si la liste est trop courte.
                        java.util.List<String> existingNames = new java.util.ArrayList<>();
                        for (com.example.quizapp_adnan.data.model.SkillDTO s : skills) {
                            existingNames.add(s.getCategorie());
                        }
                        String[][] fallbacks = {
                            {"Backend", "60"}, {"Frontend", "50"}, {"SQL", "45"},
                            {"Algo", "55"}, {"Git", "70"}
                        };
                        for (String[] fb : fallbacks) {
                            if (skills.size() >= 5) break;
                            if (!existingNames.contains(fb[0])) {
                                skills.add(new com.example.quizapp_adnan.data.model.SkillDTO(
                                        fb[0], Integer.parseInt(fb[1])));
                                existingNames.add(fb[0]);
                            }
                        }
                        skillsData.setValue(skills);
                    } else {
                        // API a répondu mais sans données : on force la valeur null
                        // pour que HomeFragment affiche le radar de démo
                        skillsData.setValue(null);
                    }
                }
                @Override
                public void onFailure(retrofit2.Call<java.util.List<com.example.quizapp_adnan.data.model.SkillDTO>> call, Throwable t) {
                    errorMessage.setValue("Erreur réseau stats");
                }
            });
        }
    }
}
