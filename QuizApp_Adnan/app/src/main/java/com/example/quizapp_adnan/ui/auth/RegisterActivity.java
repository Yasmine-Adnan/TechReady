package com.example.quizapp_adnan.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.home.HomeActivity;
import com.google.android.material.snackbar.Snackbar;

public class RegisterActivity extends AppCompatActivity {

    private RegisterViewModel viewModel;
    private EditText etName, etEmail, etPassword, etPasswordConfirm;
    private Button bRegister;
    private ImageButton btnBack, btnTogglePassword;
    private TextView tvLoginLink;

    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Champs principaux (IDs inchangés)
        etName            = findViewById(R.id.etName);
        etEmail           = findViewById(R.id.etMail);
        etPassword        = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPassword1);
        bRegister         = findViewById(R.id.bRegister);

        // Nouveaux éléments UI
        btnBack           = findViewById(R.id.btnBack);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        tvLoginLink       = findViewById(R.id.tvLoginLink);

        setupObservers();
        setupListeners();
    }

    // ─────────────────────────────────────────────────
    // Observers — logique métier inchangée
    // ─────────────────────────────────────────────────
    private void setupObservers() {
        viewModel.getRegisterState().observe(this, state -> {
            if (state instanceof RegisterViewModel.RegisterState.Success) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Inscription réussie !", Snackbar.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (state instanceof RegisterViewModel.RegisterState.Error) {
                RegisterViewModel.RegisterState.Error errorState =
                        (RegisterViewModel.RegisterState.Error) state;
                Snackbar.make(findViewById(android.R.id.content),
                        errorState.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    // ─────────────────────────────────────────────────
    // Listeners
    // ─────────────────────────────────────────────────
    private void setupListeners() {

        // Retour
        btnBack.setOnClickListener(v -> finish());

        // Toggle visibilité mot de passe
        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                btnTogglePassword.setAlpha(1.0f);
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                btnTogglePassword.setAlpha(0.5f);
            }
            // Garde le curseur en fin de texte
            etPassword.setSelection(etPassword.getText().length());
        });

        // Lien "Se connecter"
        tvLoginLink.setOnClickListener(v -> finish());

        // Bouton S'inscrire
        bRegister.setOnClickListener(v -> {
            String name    = etName.getText().toString().trim();
            String email   = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirm  = etPasswordConfirm.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Veuillez remplir tous les champs", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Le mot de passe doit faire au moins 6 caractères", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Les mots de passe ne correspondent pas", Snackbar.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(name, email, password);
        });
    }
}
