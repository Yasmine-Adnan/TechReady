package com.example.quizapp_adnan.data.model;

public class VocalSessionResponse {
    private Double score;
    private String feedback;
    private String pointsForts;
    private String aAmeliorer;

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getPointsForts() { return pointsForts; }
    public void setPointsForts(String pointsForts) { this.pointsForts = pointsForts; }
    public String getAAmeliorer() { return aAmeliorer; }
    public void setAAmeliorer(String aAmeliorer) { this.aAmeliorer = aAmeliorer; }
}
