package com.example.quizapp_adnan.ui.leaderboard;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.data.model.CampusRankResponse;
import com.example.quizapp_adnan.data.model.UserProfile;
import com.example.quizapp_adnan.data.remote.RetrofitClient;
import com.example.quizapp_adnan.data.remote.TechReadyApiService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaderboardFragment extends Fragment {

    // ── Countdown ──────────────────────────────────────
    private TextView tvDays, tvHours, tvMins, tvSecs;
    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;
    // Cible = 7 jours à partir du lancement
    private long endTimeMs = 0L;

    // ── Podium ─────────────────────────────────────────
    private TextView tvPodium1Name, tvPodium1Points;
    private TextView tvPodium2Name, tvPodium2Points;
    private TextView tvPodium3Name, tvPodium3Points;

    // ── Leaderboard principal ──────────────────────────
    private RecyclerView leaderboardRecyclerView;
    private LeaderboardAdapter adapter;
    private ProgressBar progressBar;

    // ── GPS Campus ─────────────────────────────────────
    private MaterialButton btnDetectCampus;
    private ProgressBar campusProgressBar;
    private TextView campusResultText;
    private FusedLocationProviderClient fusedLocationClient;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) detectAndSendLocation();
                        else showCampusResult("Localisation temporairement indisponible");
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        // ── Countdown views ──
        tvDays  = view.findViewById(R.id.tvCountdownDays);
        tvHours = view.findViewById(R.id.tvCountdownHours);
        tvMins  = view.findViewById(R.id.tvCountdownMins);
        tvSecs  = view.findViewById(R.id.tvCountdownSecs);

        // ── Podium views ──
        tvPodium1Name   = view.findViewById(R.id.tvPodium1Name);
        tvPodium1Points = view.findViewById(R.id.tvPodium1Points);
        tvPodium2Name   = view.findViewById(R.id.tvPodium2Name);
        tvPodium2Points = view.findViewById(R.id.tvPodium2Points);
        tvPodium3Name   = view.findViewById(R.id.tvPodium3Name);
        tvPodium3Points = view.findViewById(R.id.tvPodium3Points);

        // ── Leaderboard ──
        leaderboardRecyclerView = view.findViewById(R.id.leaderboardRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        adapter = new LeaderboardAdapter();

        // Transmettre l'UID courant pour le highlight
        String currentUid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        adapter.setCurrentUserId(currentUid);

        leaderboardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        leaderboardRecyclerView.setAdapter(adapter);
        leaderboardRecyclerView.setNestedScrollingEnabled(false);

        // ── GPS ──
        btnDetectCampus = view.findViewById(R.id.btnDetectCampus);
        campusProgressBar = view.findViewById(R.id.campusProgressBar);
        campusResultText = view.findViewById(R.id.campusResultText);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        btnDetectCampus.setOnClickListener(v -> onDetectCampusClicked());

        // ── Démarrer countdown ──
        endTimeMs = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7);
        startCountdown();

        // ── Données ──
        fetchLeaderboard();
        fetchChallenge();

        return view;
    }

    // =====================================================================
    //  COUNTDOWN TIMER
    // =====================================================================

    private void startCountdown() {
        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded() || getView() == null) return;

                long remaining = endTimeMs - System.currentTimeMillis();
                if (remaining <= 0) {
                    tvDays.setText("00");
                    tvHours.setText("00");
                    tvMins.setText("00");
                    tvSecs.setText("00");
                    return;
                }

                long days  = TimeUnit.MILLISECONDS.toDays(remaining);
                long hours = TimeUnit.MILLISECONDS.toHours(remaining) % 24;
                long mins  = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;
                long secs  = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60;

                tvDays.setText(String.format(Locale.getDefault(), "%02d", days));
                tvHours.setText(String.format(Locale.getDefault(), "%02d", hours));
                tvMins.setText(String.format(Locale.getDefault(), "%02d", mins));
                tvSecs.setText(String.format(Locale.getDefault(), "%02d", secs));

                countdownHandler.postDelayed(this, 1000);
            }
        };
        countdownHandler.post(countdownRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Éviter les memory leaks
        countdownHandler.removeCallbacks(countdownRunnable);
    }

    // =====================================================================
    //  LEADERBOARD PRINCIPAL
    // =====================================================================

    private void fetchLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("TechReadyPrefs", Context.MODE_PRIVATE);
        String filiere = prefs.getString("filiere", "Informatique");

        TechReadyApiService apiService = RetrofitClient.getApiService();
        apiService.getLeaderboard(filiere).enqueue(new Callback<List<UserProfile>>() {
            @Override
            public void onResponse(Call<List<UserProfile>> call,
                                   Response<List<UserProfile>> response) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded() || getView() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    List<UserProfile> profiles = response.body();
                    adapter.setProfiles(profiles);

                    // Populer le podium Top 3
                    updatePodium(profiles);

                    // Vérifier badge Top 10
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                            ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                            : prefs.getString("userId", "");
                    for (int i = 0; i < profiles.size(); i++) {
                        if (profiles.get(i).getUserId() != null
                                && profiles.get(i).getUserId().equals(currentUserId)) {
                            if (i < 10) {
                                com.example.quizapp_adnan.utils.BadgeManager
                                        .checkAndAwardBadge(requireContext(),
                                                currentUserId, "top_10", "Top 10");
                            }
                            break;
                        }
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Erreur de chargement du classement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<UserProfile>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Erreur de connexion", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Met à jour le podium visuellement à partir des données API */
    private void updatePodium(List<UserProfile> profiles) {
        if (profiles.size() >= 1) {
            UserProfile p1 = profiles.get(0);
            tvPodium1Name.setText(getName(p1));
            tvPodium1Points.setText(getPoints(p1));
        }
        if (profiles.size() >= 2) {
            UserProfile p2 = profiles.get(1);
            tvPodium2Name.setText(getName(p2));
            tvPodium2Points.setText(getPoints(p2));
        }
        if (profiles.size() >= 3) {
            UserProfile p3 = profiles.get(2);
            tvPodium3Name.setText(getName(p3));
            tvPodium3Points.setText(getPoints(p3));
        }
    }

    private String getName(UserProfile p) {
        String n = p.getDisplayName();
        return (n != null && !n.trim().isEmpty()) ? n : "Anonyme";
    }

    private String getPoints(UserProfile p) {
        return p.getTotalPoints() + " pts";
    }

    // =====================================================================
    //  DÉFI DE LA SEMAINE
    // =====================================================================

    private void fetchChallenge() {
        TechReadyApiService apiService = RetrofitClient.getApiService();
        apiService.getCurrentChallenge()
                .enqueue(new Callback<com.example.quizapp_adnan.data.model.Challenge>() {
                    @Override
                    public void onResponse(
                            Call<com.example.quizapp_adnan.data.model.Challenge> call,
                            Response<com.example.quizapp_adnan.data.model.Challenge> response) {
                        if (!isAdded() || getView() == null) return;
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.quizapp_adnan.data.model.Challenge challenge =
                                    response.body();

                            TextView titleText = getView().findViewById(R.id.challengeTitleText);
                            TextView descText  = getView().findViewById(R.id.challengeDescText);
                            TextView chipText  = getView().findViewById(R.id.challengePointsChip);
                            TextView emojiText = getView().findViewById(R.id.challengeEmojiText);

                            if (challenge.getEmoji() != null)
                                emojiText.setText(challenge.getEmoji());
                            if (challenge.getTitre() != null)
                                titleText.setText(challenge.getTitre().toUpperCase(Locale.getDefault()));
                            if (challenge.getDescription() != null)
                                descText.setText(challenge.getDescription());
                            chipText.setText("+" + challenge.getPointsRecompense() + " pts");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<com.example.quizapp_adnan.data.model.Challenge> call,
                            Throwable t) {
                        // Silently fail — contenu par défaut affiché
                    }
                });
    }

    // =====================================================================
    //  GPS CAMPUS
    // =====================================================================

    private void onDetectCampusClicked() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            detectAndSendLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void detectAndSendLocation() {
        btnDetectCampus.setEnabled(false);
        campusProgressBar.setVisibility(View.VISIBLE);
        campusResultText.setVisibility(View.GONE);

        LocationManager locationManager = (LocationManager)
                requireContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null
                && !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            campusProgressBar.setVisibility(View.GONE);
            btnDetectCampus.setEnabled(true);
            showCampusResult("Active le GPS dans les paramètres");
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    sendLocationToBackend(location.getLatitude(), location.getLongitude());
                } else {
                    requestLocationUpdatesWithTimeout();
                }
            }).addOnFailureListener(e -> requestLocationUpdatesWithTimeout());
        } catch (SecurityException e) {
            campusProgressBar.setVisibility(View.GONE);
            btnDetectCampus.setEnabled(true);
            showCampusResult("Localisation temporairement indisponible");
        }
    }

    private void requestLocationUpdatesWithTimeout() {
        try {
            LocationRequest req = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setMaxUpdates(1)
                    .setDurationMillis(10000)
                    .build();

            LocationCallback cb = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult result) {
                    fusedLocationClient.removeLocationUpdates(this);
                    android.location.Location loc = result.getLastLocation();
                    if (loc != null) {
                        sendLocationToBackend(loc.getLatitude(), loc.getLongitude());
                    } else {
                        if (isAdded()) {
                            campusProgressBar.setVisibility(View.GONE);
                            btnDetectCampus.setEnabled(true);
                            showCampusResult("Localisation temporairement indisponible");
                        }
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(req, cb, Looper.getMainLooper());

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                fusedLocationClient.removeLocationUpdates(cb);
                if (isAdded() && campusProgressBar.getVisibility() == View.VISIBLE) {
                    campusProgressBar.setVisibility(View.GONE);
                    btnDetectCampus.setEnabled(true);
                    showCampusResult("Localisation temporairement indisponible");
                }
            }, 10000);

        } catch (SecurityException e) {
            campusProgressBar.setVisibility(View.GONE);
            btnDetectCampus.setEnabled(true);
            showCampusResult("Localisation temporairement indisponible");
        }
    }

    private void sendLocationToBackend(double latitude, double longitude) {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("TechReadyPrefs", Context.MODE_PRIVATE);
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : prefs.getString("userId", "");

        Map<String, Object> body = new HashMap<>();
        body.put("userId", userId);
        body.put("latitude", latitude);
        body.put("longitude", longitude);

        RetrofitClient.getApiService().getCampusRank(body)
                .enqueue(new Callback<CampusRankResponse>() {
                    @Override
                    public void onResponse(Call<CampusRankResponse> call,
                                           Response<CampusRankResponse> response) {
                        if (!isAdded()) return;
                        campusProgressBar.setVisibility(View.GONE);
                        btnDetectCampus.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            CampusRankResponse rank = response.body();
                            showCampusResult("📍 Tu es " + rank.getRang()
                                    + "e sur " + rank.getTotalUsers()
                                    + " utilisateurs sur " + rank.getCampusNom());
                        } else if (response.code() == 404) {
                            showCampusResult("Aucun campus TechReady détecté à proximité");
                        } else {
                            showCampusResult("Localisation temporairement indisponible");
                        }
                    }

                    @Override
                    public void onFailure(Call<CampusRankResponse> call, Throwable t) {
                        if (!isAdded()) return;
                        campusProgressBar.setVisibility(View.GONE);
                        btnDetectCampus.setEnabled(true);
                        showCampusResult("Localisation temporairement indisponible");
                    }
                });
    }

    private void showCampusResult(String message) {
        campusResultText.setText(message);
        campusResultText.setVisibility(View.VISIBLE);
    }
}
