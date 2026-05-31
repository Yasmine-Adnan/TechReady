package com.techready.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.techready.backend.model.CampusRankResponse;
import com.techready.backend.model.CampusRequest;
import com.techready.backend.model.UserProfile;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class CampusService {

    private static final String CAMPUS_COLLECTION = "campus_locations";
    private static final String PROFILES_COLLECTION = "profiles";
    private static final double MAX_DISTANCE_METERS = 500.0;

    @Autowired
    private Firestore firestore;

    /**
     * Initialise les 5 campus TechReady dans Firestore au démarrage du serveur
     * si la collection est vide.
     */
    @PostConstruct
    public void seedCampusLocations() {
        try {
            CollectionReference col = firestore.collection(CAMPUS_COLLECTION);
            ApiFuture<QuerySnapshot> future = col.limit(1).get();
            QuerySnapshot snapshot = future.get();

            // Forcer la mise à jour si le campus_007 n'existe pas encore
            boolean needsSeeding = snapshot.isEmpty() || !col.document("campus_007").get().get().exists();

            if (needsSeeding) {
                List<Map<String, Object>> campuses = new ArrayList<>();

                campuses.add(buildCampus("campus_001", "ENSIAS Rabat", 33.9716, -6.8498));
                campuses.add(buildCampus("campus_002", "ENSA Casablanca", 33.5731, -7.5898));
                campuses.add(buildCampus("campus_003", "Université Hassan II Casablanca", 33.5892, -7.6031));
                campuses.add(buildCampus("campus_004", "INPT Rabat", 33.9989, -6.8479));
                campuses.add(buildCampus("campus_005", "EMSI Casablanca", 33.5651, -7.6342));
                campuses.add(buildCampus("campus_006", "Campus Bourgogne", 33.5954842, -7.6402436));
                campuses.add(buildCampus("campus_007", "EMSI Roudani Casablanca", 33.58105, -7.63273));

                WriteBatch batch = firestore.batch();
                for (Map<String, Object> campus : campuses) {
                    String id = (String) campus.get("id");
                    DocumentReference docRef = col.document(id);
                    batch.set(docRef, campus);
                }
                batch.commit().get();
                System.out.println("[CampusService] 7 campus TechReady initialisés dans Firestore.");
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("[CampusService] Erreur lors du seed des campus : " + e.getMessage());
        }
    }

    /**
     * Trouve le campus le plus proche de la position donnée (< 500m).
     * Si trouvé, calcule le rang de l'utilisateur parmi tous les profils.
     * @return CampusRankResponse ou null si aucun campus proche
     */
    public CampusRankResponse getCampusRank(CampusRequest request)
            throws ExecutionException, InterruptedException {

        // 1. Charger tous les campus depuis Firestore
        QuerySnapshot campusSnapshot = firestore.collection(CAMPUS_COLLECTION).get().get();

        String nearestCampusNom = null;
        double minDistance = Double.MAX_VALUE;

        for (DocumentSnapshot doc : campusSnapshot.getDocuments()) {
            Double campusLat = doc.getDouble("latitude");
            Double campusLng = doc.getDouble("longitude");
            String campusNom = doc.getString("nom");

            if (campusLat == null || campusLng == null || campusNom == null) continue;

            double distance = haversine(
                    request.getLatitude(), request.getLongitude(),
                    campusLat, campusLng
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearestCampusNom = campusNom;
            }
        }

        // 2. Si aucun campus dans 500m → retourner null
        if (nearestCampusNom == null || minDistance > MAX_DISTANCE_METERS) {
            return null;
        }

        // 3. Calculer le rang de l'utilisateur parmi tous les profils (par totalPoints)
        QuerySnapshot profilesSnapshot = firestore.collection(PROFILES_COLLECTION).get().get();
        List<Long> allPoints = new ArrayList<>();
        long userPoints = 0;

        for (DocumentSnapshot doc : profilesSnapshot.getDocuments()) {
            Long points = doc.getLong("totalPoints");
            if (points == null) points = 0L;
            allPoints.add(points);

            if (request.getUserId() != null && request.getUserId().equals(doc.getId())) {
                userPoints = points;
            }
        }

        // Trier décroissant
        allPoints.sort(Collections.reverseOrder());

        // Trouver le rang (1-based)
        int rang = 1;
        for (Long points : allPoints) {
            if (points > userPoints) rang++;
            else break;
        }

        int totalUsers = allPoints.size();
        return new CampusRankResponse(nearestCampusNom, rang, totalUsers);
    }

    // -------------------------------------------------------
    // Formule de Haversine : distance en mètres entre deux points GPS
    // -------------------------------------------------------
    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6_371_000; // rayon de la Terre en mètres
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Map<String, Object> buildCampus(String id, String nom, double lat, double lng) {
        Map<String, Object> campus = new HashMap<>();
        campus.put("id", id);
        campus.put("nom", nom);
        campus.put("latitude", lat);
        campus.put("longitude", lng);
        return campus;
    }
}
