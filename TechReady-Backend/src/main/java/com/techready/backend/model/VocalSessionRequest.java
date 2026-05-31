package com.techready.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VocalSessionRequest {
    private String questionId;
    private String userId;
    private String reponseText;
    
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getReponseText() { return reponseText; }
    public void setReponseText(String reponseText) { this.reponseText = reponseText; }
}
