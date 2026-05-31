package com.example.quizapp_adnan.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.data.remote.SeedDataManager;
import com.example.quizapp_adnan.ui.home.HomeActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;
    private EditText etEmail, etPassword;
    private Button bLogin;
    private TextView tvRegister;
    private ImageButton ibTogglePassword, ibBack;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialiser/vérifier la BDD (Seed)
        new SeedDataManager().seedIfNeeded();

        // 2. Persistance de session (si déjà connecté -> HomeActivity)
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        etEmail = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        bLogin = findViewById(R.id.bLogin);
        tvRegister = findViewById(R.id.tvRegister);
        ibTogglePassword = findViewById(R.id.ibTogglePassword);
        ibBack = findViewById(R.id.ibBack);

        setupObservers();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getLoginState().observe(this, state -> {
            if (state instanceof LoginViewModel.LoginState.Success) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else if (state instanceof LoginViewModel.LoginState.Error) {
                String error = ((LoginViewModel.LoginState.Error) state).getMessage();
                Snackbar.make(findViewById(android.R.id.content), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupListeners() {
        // Connexion
        bLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Veuillez remplir tous les champs", Snackbar.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(email, password);
        });

        // Inscription
        tvRegister.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        // Retour
        ibBack.setOnClickListener(v -> onBackPressed());

        // Toggle visibilité mot de passe
        ibTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                ibTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                ibTogglePassword.setImageResource(android.R.drawable.ic_menu_view);
            }
            // Maintenir le curseur en fin de saisie
            etPassword.setSelection(etPassword.getText().length());
        });
    }
}
