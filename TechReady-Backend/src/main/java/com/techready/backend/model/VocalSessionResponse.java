package com.techready.backend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocalSessionResponse {
    private Double score;
    private String feedback;
    private String pointsForts;
    
    @JsonProperty("aAmeliorer")
    private String aAmeliorer;
    
    public VocalSessionResponse() {
    }

    public VocalSessionResponse(Double score, String feedback, String pointsForts, String aAmeliorer) {
        this.score = score;
        this.feedback = feedback;
        this.pointsForts = pointsForts;
        this.aAmeliorer = aAmeliorer;
    }
    
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public String getPointsForts() { return pointsForts; }
    public void setPointsForts(String pointsForts) { this.pointsForts = pointsForts; }
    public String getAAmeliorer() { return aAmeliorer; }
    public void setAAmeliorer(String aAmeliorer) { this.aAmeliorer = aAmeliorer; }
}
