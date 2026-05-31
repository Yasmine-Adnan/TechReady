package com.techready.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestion {
    private String id;

    @NotBlank(message = "La filière est requise")
    private String filiere;

    @NotBlank(message = "La spécialité est requise")
    private String specialite;

    @NotBlank(message = "Le niveau est requis")
    private String niveau;

    @NotBlank(message = "La question est requise")
    private String question;

    @NotEmpty(message = "Les options sont requises")
    private List<String> options;

    @NotNull(message = "L'index de la réponse correcte est requis")
    @Min(value = 0, message = "L'index doit être positif")
    private Integer correctIndex;

    private String explanation;
    private List<String> tags;
    private String difficulty;

    private Boolean isVocalAccessible;
    private String bonneReponseComplete;
    private String entrepriseTag;
    private String localisationTag;

    // --- Getters et Setters explicites de secours ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFiliere() { return filiere; }
    public void setFiliere(String filiere) { this.filiere = filiere; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public Integer getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(Integer correctIndex) { this.correctIndex = correctIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Boolean getIsVocalAccessible() { return isVocalAccessible; }
    public void setIsVocalAccessible(Boolean isVocalAccessible) { this.isVocalAccessible = isVocalAccessible; }

    public String getBonneReponseComplete() { return bonneReponseComplete; }
    public void setBonneReponseComplete(String bonneReponseComplete) { this.bonneReponseComplete = bonneReponseComplete; }

    public String getEntrepriseTag() { return entrepriseTag; }
    public void setEntrepriseTag(String entrepriseTag) { this.entrepriseTag = entrepriseTag; }

    public String getLocalisationTag() { return localisationTag; }
    public void setLocalisationTag(String localisationTag) { this.localisationTag = localisationTag; }
}
