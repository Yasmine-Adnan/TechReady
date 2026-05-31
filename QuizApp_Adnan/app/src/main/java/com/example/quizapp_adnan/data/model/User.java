package com.example.quizapp_adnan.data.model;

import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class User {
    private String userId;
    private String displayName;
    private String email;
    private Date createdAt;
    private int totalSessions;
    private int lastScore;
    private int bestScore;
    private Map<String, Object> lastProfile;
    private List<String> badges;

    // Constructeur vide requis par Firestore
    public User() {}

    public User(String userId, String displayName, String email, Date createdAt) {
        this.userId = userId;
        this.displayName = displayName;
        this.email = email;
        this.createdAt = createdAt;
        this.totalSessions = 0;
        this.lastScore = 0;
        this.bestScore = 0;
        this.badges = new ArrayList<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }

    public int getLastScore() { return lastScore; }
    public void setLastScore(int lastScore) { this.lastScore = lastScore; }

    public int getBestScore() { return bestScore; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }

    public Map<String, Object> getLastProfile() { return lastProfile; }
    public void setLastProfile(Map<String, Object> lastProfile) { this.lastProfile = lastProfile; }

    public List<String> getBadges() { return badges; }
    public void setBadges(List<String> badges) { this.badges = badges; }
}
