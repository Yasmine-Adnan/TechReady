package com.example.quizapp_adnan.data.model;

import java.util.List;

public class InterviewQuestion {
    private String id;
    private String filiere;
    private String specialite;
    private String niveau;
    private String question;
    private List<String> options;
    private int correctIndex;
    private String explanation;
    private List<String> tags;
    private String difficulty;
    private Boolean isVocalAccessible;
    private String bonneReponseComplete;
    private String entrepriseTag;
    private String localisationTag;

    // Constructeur vide requis par Firestore
    public InterviewQuestion() {}

    public InterviewQuestion(String id, String filiere, String specialite, String niveau, String question, List<String> options, int correctIndex, String explanation, List<String> tags, String difficulty) {
        this.id = id;
        this.filiere = filiere;
        this.specialite = specialite;
        this.niveau = niveau;
        this.question = question;
        this.options = options;
        this.correctIndex = correctIndex;
        this.explanation = explanation;
        this.tags = tags;
        this.difficulty = difficulty;
    }

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

    public int getCorrectIndex() { return correctIndex; }
    public void setCorrectIndex(int correctIndex) { this.correctIndex = correctIndex; }

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
