package com.example.quizapp_adnan.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "flashcards")
public class FlashcardEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String category;
    private String question;
    private String answer;
    private boolean isKnown;

    public FlashcardEntity(String category, String question, String answer) {
        this.category = category;
        this.question = question;
        this.answer = answer;
        this.isKnown = false; // Par défaut, non maîtrisé
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public boolean isKnown() { return isKnown; }
    public void setKnown(boolean known) { isKnown = known; }
}
