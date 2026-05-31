package com.example.quizapp_adnan.data.model;

public class VocalSessionRequest {
    private String questionId;
    private String userId;
    private String reponseText;

    public VocalSessionRequest(String questionId, String userId, String reponseText) {
        this.questionId = questionId;
        this.userId = userId;
        this.reponseText = reponseText;
    }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getReponseText() { return reponseText; }
    public void setReponseText(String reponseText) { this.reponseText = reponseText; }
}
