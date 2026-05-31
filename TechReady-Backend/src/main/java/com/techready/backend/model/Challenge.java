package com.techready.backend.model;

public class Challenge {
    private String id;
    private String titre;
    private String description;
    private String categorie;
    private int pointsRecompense;
    private String dateDebut;
    private String dateFin;
    private String emoji;

    public Challenge() {
    }

    public Challenge(String id, String titre, String description, String categorie, int pointsRecompense, String dateDebut, String dateFin, String emoji) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.categorie = categorie;
        this.pointsRecompense = pointsRecompense;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.emoji = emoji;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public int getPointsRecompense() { return pointsRecompense; }
    public void setPointsRecompense(int pointsRecompense) { this.pointsRecompense = pointsRecompense; }
    public String getDateDebut() { return dateDebut; }
    public void setDateDebut(String dateDebut) { this.dateDebut = dateDebut; }
    public String getDateFin() { return dateFin; }
    public void setDateFin(String dateFin) { this.dateFin = dateFin; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}
