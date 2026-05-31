package com.techready.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillDTO {
    private String categorie;
    private int pourcentage;

    // --- Constructeurs explicites (pour contrer l'échec de Lombok) ---
    public SkillDTO() {
    }

    public SkillDTO(String categorie, int pourcentage) {
        this.categorie = categorie;
        this.pourcentage = pourcentage;
    }

    // --- Getters et Setters explicites ---
    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getPourcentage() {
        return pourcentage;
    }

    public void setPourcentage(int pourcentage) {
        this.pourcentage = pourcentage;
    }
}
