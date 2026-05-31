package com.example.quizapp_adnan.data.model;

public class SkillDTO {
    private String categorie;
    private int pourcentage;

    public SkillDTO() {}

    public SkillDTO(String categorie, int pourcentage) {
        this.categorie = categorie;
        this.pourcentage = pourcentage;
    }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public int getPourcentage() { return pourcentage; }
    public void setPourcentage(int pourcentage) { this.pourcentage = pourcentage; }
}
