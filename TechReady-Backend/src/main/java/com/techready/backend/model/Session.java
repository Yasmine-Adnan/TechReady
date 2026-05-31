package com.techready.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    @NotBlank(message = "L'ID de l'utilisateur est requis")
    private String userId;

    @NotBlank(message = "L'ID du profil est requis")
    private String profileId;

    @NotNull(message = "Le score est requis")
    @Min(value = 0, message = "Le score ne peut pas être négatif")
    private Integer score;

    @NotNull(message = "Le total est requis")
    @Min(value = 1, message = "Le total doit être au moins 1")
    private Integer total;

    @NotNull(message = "Le pourcentage est requis")
    private Double percentage;

    @NotNull(message = "Le temps pris est requis")
    @Min(value = 0, message = "Le temps pris ne peut pas être négatif")
    private Integer timeTakenSeconds;

    private Date date;

    private List<Map<String, Object>> questionsAnswered;

    private String typeMode;
    private List<String> categoriesEchouees = new ArrayList<>();
    private String feedbackIA;
    private Double scoreVocal;

    // --- Getters et Setters explicites de secours ---
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public Integer getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(Integer timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public List<Map<String, Object>> getQuestionsAnswered() {
        return questionsAnswered;
    }

    public void setQuestionsAnswered(List<Map<String, Object>> questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
    }

    public String getTypeMode() { return typeMode; }
    public void setTypeMode(String typeMode) { this.typeMode = typeMode; }

    public List<String> getCategoriesEchouees() { return categoriesEchouees; }
    public void setCategoriesEchouees(List<String> categoriesEchouees) { this.categoriesEchouees = categoriesEchouees; }

    public String getFeedbackIA() { return feedbackIA; }
    public void setFeedbackIA(String feedbackIA) { this.feedbackIA = feedbackIA; }

    public Double getScoreVocal() { return scoreVocal; }
    public void setScoreVocal(Double scoreVocal) { this.scoreVocal = scoreVocal; }
}
