package com.example.quizapp_adnan.data.model;

import java.util.Date;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Session {
    private String userId;
    private String profileId;
    private int score;
    private int total;
    private double percentage;
    private int timeTakenSeconds;
    private Date date;
    private List<Map<String, Object>> questionsAnswered;

    private String typeMode;
    private List<String> categoriesEchouees = new ArrayList<>();
    private String feedbackIA;
    private Double scoreVocal;

    // Constructeur vide requis par Firestore
    public Session() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProfileId() { return profileId; }
    public void setProfileId(String profileId) { this.profileId = profileId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public int getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(int timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public List<Map<String, Object>> getQuestionsAnswered() { return questionsAnswered; }
    public void setQuestionsAnswered(List<Map<String, Object>> questionsAnswered) { this.questionsAnswered = questionsAnswered; }

    public String getTypeMode() { return typeMode; }
    public void setTypeMode(String typeMode) { this.typeMode = typeMode; }

    public List<String> getCategoriesEchouees() { return categoriesEchouees; }
    public void setCategoriesEchouees(List<String> categoriesEchouees) { this.categoriesEchouees = categoriesEchouees; }

    public String getFeedbackIA() { return feedbackIA; }
    public void setFeedbackIA(String feedbackIA) { this.feedbackIA = feedbackIA; }

    public Double getScoreVocal() { return scoreVocal; }
    public void setScoreVocal(Double scoreVocal) { this.scoreVocal = scoreVocal; }
}
