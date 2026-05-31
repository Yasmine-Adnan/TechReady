package com.example.quizapp_adnan.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class LoginViewModel extends ViewModel {
    private final FirebaseAuth mAuth;
    private final MutableLiveData<LoginState> loginState = new MutableLiveData<>();

    public LoginViewModel() {
        mAuth = FirebaseAuth.getInstance();
    }

    public LiveData<LoginState> getLoginState() {
        return loginState;
    }

    public void login(String email, String password) {
        loginState.setValue(new LoginState.Loading());
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loginState.setValue(new LoginState.Success());
                    } else {
                        loginState.setValue(new LoginState.Error(task.getException() != null ? task.getException().getMessage() : "Erreur de connexion"));
                    }
                });
    }

    public static abstract class LoginState {
        public static class Loading extends LoginState {}
        public static class Success extends LoginState {}
        public static class Error extends LoginState {
            private final String message;
            public Error(String message) { this.message = message; }
            public String getMessage() { return message; }
        }
    }
}
