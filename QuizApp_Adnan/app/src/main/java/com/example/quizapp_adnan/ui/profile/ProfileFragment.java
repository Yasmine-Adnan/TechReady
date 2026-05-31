package com.example.quizapp_adnan.ui.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.auth.LoginActivity;
import com.example.quizapp_adnan.ui.profiling.ProfilingActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements OnMapReadyCallback {

    // ── SharedPreferences keys ──
    private static final String PREFS_NAME       = "ProfilePrefs";
    private static final String KEY_PHOTO_PATH   = "profile_photo_path";
    private static final String KEY_CITY_NAME    = "last_city_name";
    private static final String KEY_CITY_LAT     = "last_city_lat";
    private static final String KEY_CITY_LNG     = "last_city_lng";

    private static final String FILE_PROVIDER_AUTH = "com.example.quizapp_adnan.fileprovider";

    // ── ViewModel ──
    private ProfileViewModel profileViewModel;

    // ── Photo vues ──
    private FrameLayout   profileImageContainer;
    private CircleImageView profileImage;

    // ── Profil vues ──
    private TextView tvName, tvEmail, tvFiliere, tvSpecialite, tvLevel, tvXp,
                     tvNoBadges, tvBadgeCount, tvProfileBadgeChip;
    private RecyclerView badgesRecyclerView;
    private BadgeAdapter badgeAdapter;
    private Button btnEditProfile, btnLogout;

    // ── Localisation vues ──
    private TextView tvCityName;
    private Button   btnDetectLocation, btnRefreshLocation;
    private MapView  mapView;
    private GoogleMap googleMap;

    // ── GPS ──
    private FusedLocationProviderClient fusedLocationClient;

    // ── URI temporaire caméra ──
    private Uri cameraImageUri;

    // ── ActivityResult launchers ──
    private ActivityResultLauncher<Uri>      launcherCamera;
    private ActivityResultLauncher<String>   launcherGallery;
    private ActivityResultLauncher<String>   launcherCameraPermission;
    private ActivityResultLauncher<String[]> launcherLocationPermission;

    // ── Dictionnaire badges ──
    private static final Map<String, BadgeInfo> BADGE_DICTIONARY = new HashMap<String, BadgeInfo>() {{
        put("premier_pas",  new BadgeInfo("🏅", "Premier Pas"));
        put("en_feu",       new BadgeInfo("🔥", "En feu"));
        put("survivant",    new BadgeInfo("💀", "Survivant"));
        put("orateur",      new BadgeInfo("🎤", "Orateur"));
        put("assidu",       new BadgeInfo("📅", "Assidu"));
        put("top_10",       new BadgeInfo("🏆", "Top 10"));
        put("ninja",        new BadgeInfo("🥷", "Ninja"));
        put("explorateur",  new BadgeInfo("🧭", "Explorateur"));
        put("champion",     new BadgeInfo("🥇", "Champion"));
        put("legende",      new BadgeInfo("👑", "Légende"));
    }};

    private static final List<String> ALL_BADGE_KEYS = Arrays.asList(
            "premier_pas", "en_feu", "survivant",
            "orateur", "assidu", "top_10",
            "ninja", "explorateur", "champion", "legende"
    );
    private static final int TOTAL_BADGES = 10;

    // ────────────────────────────────────────────────────────────
    //  Enregistrement des launchers AVANT onCreateView
    // ────────────────────────────────────────────────────────────

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Caméra
        launcherCamera = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> { if (success && cameraImageUri != null) displayAndSavePhoto(cameraImageUri); }
        );

        // Galerie
        launcherGallery = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> { if (uri != null) displayAndSavePhoto(uri); }
        );

        // Permission caméra
        launcherCameraPermission = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) openCamera();
                    else Toast.makeText(requireContext(), "Permission caméra refusée", Toast.LENGTH_SHORT).show();
                }
        );

        // Permission localisation (Fine + Coarse)
        launcherLocationPermission = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                            || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (granted) fetchCurrentLocation();
                    else Toast.makeText(requireContext(), "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
                }
        );
    }

    // ────────────────────────────────────────────────────────────
    //  onCreateView
    // ────────────────────────────────────────────────────────────

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // ── Binding photo + profil ──
        profileImageContainer = view.findViewById(R.id.profileImageContainer);
        profileImage          = view.findViewById(R.id.profileImage);
        tvName                = view.findViewById(R.id.profileName);
        tvEmail               = view.findViewById(R.id.profileEmail);
        tvFiliere             = view.findViewById(R.id.profileFiliere);
        tvSpecialite          = view.findViewById(R.id.profileSpecialite);
        tvLevel               = view.findViewById(R.id.profileLevel);
        tvXp                  = view.findViewById(R.id.profileXp);
        tvNoBadges            = view.findViewById(R.id.noBadgesText);
        tvBadgeCount          = view.findViewById(R.id.tvBadgeCount);
        tvProfileBadgeChip    = view.findViewById(R.id.tvProfileBadgeChip);
        badgesRecyclerView    = view.findViewById(R.id.badgesRecyclerView);
        btnEditProfile        = view.findViewById(R.id.btnEditProfile);
        btnLogout             = view.findViewById(R.id.btnLogout);

        // ── Binding localisation ──
        tvCityName          = view.findViewById(R.id.tvCityName);
        btnDetectLocation   = view.findViewById(R.id.btnDetectLocation);
        btnRefreshLocation  = view.findViewById(R.id.btnRefreshLocation);
        mapView             = view.findViewById(R.id.mapView);

        // ── Init MapView avec le bundle de savedInstanceState ──
        mapView.onCreate(savedInstanceState);

        // ── Setup RecyclerView badges ──
        badgeAdapter = new BadgeAdapter(new ArrayList<>());
        badgesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        badgesRecyclerView.setAdapter(badgeAdapter);
        badgesRecyclerView.setNestedScrollingEnabled(false);

        loadSavedPhoto();
        restoreSavedLocation();

        observeViewModel();
        setupListeners();
        profileViewModel.loadUserProfile();

        return view;
    }

    // ────────────────────────────────────────────────────────────
    //  Cycle de vie MapView — OBLIGATOIRE
    // ────────────────────────────────────────────────────────────

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // ────────────────────────────────────────────────────────────
    //  OnMapReadyCallback
    // ────────────────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    // ────────────────────────────────────────────────────────────
    //  Listeners
    // ────────────────────────────────────────────────────────────

    private void setupListeners() {

        // Avatar → BottomSheet
        profileImageContainer.setOnClickListener(v -> showPhotoPickerSheet());

        // Détecter ma position
        btnDetectLocation.setOnClickListener(v -> checkLocationPermissionAndFetch());

        // Relancer GPS
        btnRefreshLocation.setOnClickListener(v -> checkLocationPermissionAndFetch());

        // Modifier profil
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProfilingActivity.class);
            intent.putExtra("IS_EDIT_MODE", true);
            startActivity(intent);
        });

        // Déconnexion
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finishAffinity();
        });
    }

    // ────────────────────────────────────────────────────────────
    //  Localisation GPS
    // ────────────────────────────────────────────────────────────

    private void checkLocationPermissionAndFetch() {
        boolean hasFine = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasCoarse = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (hasFine || hasCoarse) {
            fetchCurrentLocation();
        } else {
            launcherLocationPermission.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchCurrentLocation() {
        btnDetectLocation.setEnabled(false);
        btnDetectLocation.setText("Localisation en cours...");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    btnDetectLocation.setEnabled(true);
                    btnDetectLocation.setText("🔍  Détecter ma ville");

                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        resolveCity(lat, lng);
                    } else {
                        Toast.makeText(requireContext(),
                                "Position introuvable. Active le GPS et réessaie.",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    btnDetectLocation.setEnabled(true);
                    btnDetectLocation.setText("🔍  Détecter ma ville");
                    Toast.makeText(requireContext(),
                            "Erreur GPS : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /** Convertit lat/lng en nom de ville via Geocoder, met à jour la carte et persiste */
    private void resolveCity(double lat, double lng) {
        String cityName = "Ville inconnue";

        try {
            Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                if (addr.getLocality() != null)       cityName = addr.getLocality();
                else if (addr.getSubAdminArea() != null) cityName = addr.getSubAdminArea();
                else if (addr.getAdminArea() != null)    cityName = addr.getAdminArea();
            }
        } catch (IOException | IllegalStateException e) {
            // Geocoder indisponible — on affiche les coordonnées brutes
            cityName = String.format(Locale.getDefault(), "%.4f, %.4f", lat, lng);
        }

        // Sauvegarder
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(KEY_CITY_NAME, cityName)
                .putFloat(KEY_CITY_LAT, (float) lat)
                .putFloat(KEY_CITY_LNG, (float) lng)
                .apply();

        // Afficher
        showLocationOnMap(cityName, lat, lng);
    }

    /** Affiche la carte avec un marqueur et le nom de la ville */
    private void showLocationOnMap(String cityName, double lat, double lng) {
        // Afficher les vues cachées
        tvCityName.setVisibility(View.VISIBLE);
        tvCityName.setText("📍 " + cityName);
        mapView.setVisibility(View.VISIBLE);
        btnRefreshLocation.setVisibility(View.VISIBLE);
        btnDetectLocation.setVisibility(View.GONE);

        LatLng position = new LatLng(lat, lng);

        // Initialiser la carte si elle n'est pas encore prête
        mapView.getMapAsync(map -> {
            this.googleMap = map;
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(position).title(cityName));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 13f));
        });
    }

    /** Restaure la dernière localisation sauvegardée depuis SharedPreferences */
    private void restoreSavedLocation() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cityName = prefs.getString(KEY_CITY_NAME, null);
        float lat = prefs.getFloat(KEY_CITY_LAT, Float.MIN_VALUE);
        float lng = prefs.getFloat(KEY_CITY_LNG, Float.MIN_VALUE);

        if (cityName != null && lat != Float.MIN_VALUE) {
            showLocationOnMap(cityName, lat, lng);
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Photo de profil (Étape 1 — INCHANGÉ)
    // ────────────────────────────────────────────────────────────

    private void showPhotoPickerSheet() {
        BottomSheetDialog sheet = new BottomSheetDialog(requireContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_photo, null);
        sheet.setContentView(sheetView);
        sheetView.findViewById(R.id.optionCamera).setOnClickListener(v -> {
            sheet.dismiss();
            checkCameraPermissionAndOpen();
        });
        sheetView.findViewById(R.id.optionGallery).setOnClickListener(v -> {
            sheet.dismiss();
            launcherGallery.launch("image/*");
        });
        sheet.show();
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            launcherCameraPermission.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            File photoFile = createTempPhotoFile();
            cameraImageUri = FileProvider.getUriForFile(requireContext(), FILE_PROVIDER_AUTH, photoFile);
            launcherCamera.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Impossible d'ouvrir la caméra", Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempPhotoFile() throws IOException {
        File dir = new File(requireContext().getCacheDir(), "profile_photos");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "profile_" + System.currentTimeMillis() + ".jpg");
    }

    private void displayAndSavePhoto(Uri uri) {
        Glide.with(this).load(uri).circleCrop()
                .placeholder(R.drawable.baseline_person_28).into(profileImage);
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putString(KEY_PHOTO_PATH, uri.toString()).apply();
    }

    private void loadSavedPhoto() {
        String saved = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_PHOTO_PATH, null);
        if (saved != null) {
            Glide.with(this).load(Uri.parse(saved)).circleCrop()
                    .placeholder(R.drawable.baseline_person_28)
                    .error(R.drawable.baseline_person_28).into(profileImage);
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Observer ViewModel — INCHANGÉ
    // ────────────────────────────────────────────────────────────

    private void observeViewModel() {
        profileViewModel.getUserProfileLiveData().observe(getViewLifecycleOwner(), doc -> {
            if (doc != null && doc.exists()) {
                String displayName = doc.getString("displayName");
                tvName.setText(displayName != null ? displayName : "—");
                tvEmail.setText(doc.getString("email") != null ? doc.getString("email") : "—");

                Map<String, Object> lastProfile = (Map<String, Object>) doc.get("lastProfile");
                if (lastProfile != null) {
                    tvFiliere.setText(lastProfile.containsKey("filiere") ? (String) lastProfile.get("filiere") : "Non définie");
                    tvSpecialite.setText(lastProfile.containsKey("specialite") ? (String) lastProfile.get("specialite") : "Non définie");
                } else {
                    tvFiliere.setText("Non définie");
                    tvSpecialite.setText("Non définie");
                }

                Long totalPoints = doc.getLong("totalPoints");
                long xp = totalPoints != null ? totalPoints : 0;
                tvXp.setText(xp + " XP");
                int level = profileViewModel.calculateLevel(xp);
                tvLevel.setText("Niveau " + level);
                tvProfileBadgeChip.setText(getLevelTitle(level));

                List<String> earnedBadgeKeys = (List<String>) doc.get("badges");
                Set<String> earnedSet = earnedBadgeKeys != null ? new HashSet<>(earnedBadgeKeys) : new HashSet<>();
                tvBadgeCount.setText(earnedSet.size() + "/" + TOTAL_BADGES);

                List<BadgeItem> allBadges = new ArrayList<>();
                for (String key : ALL_BADGE_KEYS) {
                    BadgeInfo info = BADGE_DICTIONARY.get(key);
                    if (info != null) {
                        boolean unlocked = earnedSet.contains(key);
                        allBadges.add(new BadgeItem(unlocked ? info.emoji : "🔒", info.name, unlocked));
                    }
                }
                badgesRecyclerView.setVisibility(View.VISIBLE);
                tvNoBadges.setVisibility(View.GONE);
                badgeAdapter.setBadges(allBadges);
            }
        });
    }

    private String getLevelTitle(int level) {
        if (level <= 2) return "Junior Student";
        if (level <= 4) return "Student";
        if (level <= 6) return "Senior Student";
        if (level <= 8) return "Expert";
        return "Master";
    }

    // ────────────────────────────────────────────────────────────
    //  Adapter badges — INCHANGÉ
    // ────────────────────────────────────────────────────────────

    private static class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {
        private List<BadgeItem> badges;
        BadgeAdapter(List<BadgeItem> badges) { this.badges = badges; }
        void setBadges(List<BadgeItem> b) { this.badges = b; notifyDataSetChanged(); }

        @NonNull @Override
        public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
            return new BadgeViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull BadgeViewHolder h, int pos) {
            BadgeItem item = badges.get(pos);
            h.tvEmoji.setText(item.emoji);
            h.tvName.setText(item.name);
            h.itemView.setAlpha(item.unlocked ? 1.0f : 0.45f);
            h.tvName.setTextColor(item.unlocked ? 0xFF6B6E8A : 0xFFBBBBBB);
        }
        @Override public int getItemCount() { return badges != null ? badges.size() : 0; }

        static class BadgeViewHolder extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName;
            BadgeViewHolder(@NonNull View v) {
                super(v);
                tvEmoji = v.findViewById(R.id.badgeEmoji);
                tvName  = v.findViewById(R.id.badgeName);
            }
        }
    }

    // ────────────────────────────────────────────────────────────
    //  Modèles internes
    // ────────────────────────────────────────────────────────────

    private static class BadgeInfo {
        final String emoji, name;
        BadgeInfo(String emoji, String name) { this.emoji = emoji; this.name = name; }
    }

    private static class BadgeItem {
        final String emoji, name;
        final boolean unlocked;
        BadgeItem(String emoji, String name, boolean unlocked) {
            this.emoji = emoji; this.name = name; this.unlocked = unlocked;
        }
    }
}
