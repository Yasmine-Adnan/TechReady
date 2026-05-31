package com.example.quizapp_adnan.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.quizapp_adnan.data.model.User;
import com.example.quizapp_adnan.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Date;

public class RegisterViewModel extends ViewModel {
    private final FirebaseAuth mAuth;
    private final UserRepository userRepository;
    private final MutableLiveData<RegisterState> registerState = new MutableLiveData<>();

    public RegisterViewModel() {
        mAuth = FirebaseAuth.getInstance();
        userRepository = new UserRepository();
    }

    public LiveData<RegisterState> getRegisterState() {
        return registerState;
    }

    public void register(String name, String email, String password) {
        registerState.setValue(new RegisterState.Loading());
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fUser = mAuth.getCurrentUser();
                        if (fUser != null) {
                            // Update FirebaseAuth profile to store displayName
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();
                            fUser.updateProfile(profileUpdates);

                            User user = new User(fUser.getUid(), name, email, new Date());
                            // Sauvegarder l'utilisateur dans Firestore
                            userRepository.saveUser(user).addOnCompleteListener(saveTask -> {
                                if (saveTask.isSuccessful()) {
                                    registerState.setValue(new RegisterState.Success());
                                } else {
                                    registerState.setValue(new RegisterState.Error("Erreur lors de la création du profil BDD."));
                                }
                            });
                        }
                    } else {
                        registerState.setValue(new RegisterState.Error(task.getException() != null ? task.getException().getMessage() : "Erreur d'inscription"));
                    }
                });
    }

    public static abstract class RegisterState {
        public static class Loading extends RegisterState {}
        public static class Success extends RegisterState {}
        public static class Error extends RegisterState {
            private final String message;
            public Error(String message) { this.message = message; }
            public String getMessage() { return message; }
        }
    }
}
