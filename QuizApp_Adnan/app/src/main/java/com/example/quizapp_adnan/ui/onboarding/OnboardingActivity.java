package com.example.quizapp_adnan.ui.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.auth.LoginActivity;
import com.example.quizapp_adnan.ui.auth.RegisterActivity;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private Button btnNext;
    private LinearLayout layoutAuthButtons;
    private Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        layoutAuthButtons = findViewById(R.id.layoutAuthButtons);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 2) {
                    btnNext.setVisibility(View.GONE);
                    layoutAuthButtons.setVisibility(View.VISIBLE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    layoutAuthButtons.setVisibility(View.GONE);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < 2) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        btnLogin.setOnClickListener(v -> finishOnboardingAndGoToAuth(LoginActivity.class));
        btnRegister.setOnClickListener(v -> finishOnboardingAndGoToAuth(RegisterActivity.class));
    }

    private void finishOnboardingAndGoToAuth(Class<?> targetActivity) {
        SharedPreferences prefs = getSharedPreferences("techready_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("onboarding_complete", true).apply();

        Intent intent = new Intent(OnboardingActivity.this, targetActivity);
        startActivity(intent);
        finish();
    }
}
