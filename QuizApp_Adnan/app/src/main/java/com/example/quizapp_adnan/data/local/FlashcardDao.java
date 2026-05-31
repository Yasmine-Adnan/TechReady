package com.example.quizapp_adnan.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE isKnown = 0 ORDER BY RANDOM() LIMIT 10")
    LiveData<List<FlashcardEntity>> getRandomUnknownFlashcards();

    @Query("SELECT * FROM flashcards WHERE category = :category AND isKnown = 0")
    LiveData<List<FlashcardEntity>> getUnknownFlashcardsByCategory(String category);

    @Insert
    void insertAll(List<FlashcardEntity> flashcards);

    @Update
    void updateFlashcard(FlashcardEntity flashcard);

    @Query("UPDATE flashcards SET isKnown = 0")
    void resetAllFlashcards();

    @Query("SELECT COUNT(*) FROM flashcards")
    int getCount();
}
