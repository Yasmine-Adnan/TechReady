package com.example.quizapp_adnan.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.quizapp_adnan.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navView, navController);
        }

        checkAssiduBadge();
    }

    private void checkAssiduBadge() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        SharedPreferences prefs = getSharedPreferences("TechReadyPrefs", MODE_PRIVATE);
        String lastLoginDateStr = prefs.getString("lastLoginDate_" + userId, "");
        int consecutiveDays = prefs.getInt("consecutiveDays_" + userId, 0);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayStr = sdf.format(new Date());

        if (todayStr.equals(lastLoginDateStr)) {
            // Déjà connecté aujourd'hui, on ne fait rien
            return;
        }

        // Vérifier si c'était hier
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterdayStr = sdf.format(cal.getTime());

        if (yesterdayStr.equals(lastLoginDateStr)) {
            consecutiveDays++;
        } else {
            consecutiveDays = 1;
        }

        prefs.edit()
                .putString("lastLoginDate_" + userId, todayStr)
                .putInt("consecutiveDays_" + userId, consecutiveDays)
                .apply();

        if (consecutiveDays == 5) {
            com.example.quizapp_adnan.utils.BadgeManager.checkAndAwardBadge(this, userId, "assidu", "Assidu");
        }
    }
}
