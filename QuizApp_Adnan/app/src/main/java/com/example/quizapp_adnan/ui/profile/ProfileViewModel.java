package com.example.quizapp_adnan.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizapp_adnan.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;
    
    private final MutableLiveData<DocumentSnapshot> userProfileLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public ProfileViewModel() {
        this.userRepository = new UserRepository();
    }

    public LiveData<DocumentSnapshot> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            errorLiveData.setValue("Utilisateur non connecté");
            return;
        }

        isLoading.setValue(true);
        userRepository.getUser(currentUser.getUid())
                .addOnSuccessListener(documentSnapshot -> {
                    isLoading.setValue(false);
                    if (documentSnapshot.exists()) {
                        userProfileLiveData.setValue(documentSnapshot);
                    } else {
                        errorLiveData.setValue("Profil introuvable");
                    }
                })
                .addOnFailureListener(e -> {
                    isLoading.setValue(false);
                    errorLiveData.setValue("Erreur réseau: " + e.getMessage());
                });
    }

    /**
     * Calcule le niveau en fonction de l'XP total (totalPoints)
     * Exemple de formule : Niveau = 1 + (XP / 100)
     */
    public int calculateLevel(long totalXp) {
        return 1 + (int) (totalXp / 100);
    }
}
