package com.techready.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    @NotBlank(message = "L'ID de l'utilisateur est requis")
    private String userId;

    @NotBlank(message = "La filière est requise")
    private String filiere;

    @NotBlank(message = "La spécialité est requise")
    private String specialite;

    private List<String> technos;

    private String niveau;

    @NotBlank(message = "Le type de contrat est requis")
    private String typeContrat;

    private Date createdAt;
    
    private String objectif;
    private boolean panicMode;
    
    // Nouveaux champs pour le Leaderboard
    private int totalPoints;
    private int bestScore;
    private int totalSessions;
    private String displayName;

    // --- Getters et Setters explicites complets (pour contrer l'échec de Lombok) ---
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public List<String> getTechnos() { return technos; }
    public void setTechnos(List<String> technos) { this.technos = technos; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getTypeContrat() { return typeContrat; }
    public void setTypeContrat(String typeContrat) { this.typeContrat = typeContrat; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public String getObjectif() { return objectif; }
    public void setObjectif(String objectif) { this.objectif = objectif; }

    public boolean isPanicMode() { return panicMode; }
    public void setPanicMode(boolean panicMode) { this.panicMode = panicMode; }
    
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
