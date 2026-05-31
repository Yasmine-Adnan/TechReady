package com.example.quizapp_adnan.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.quizapp_adnan.data.local.AppDatabase;
import com.example.quizapp_adnan.data.local.FlashcardDao;
import com.example.quizapp_adnan.data.local.FlashcardEntity;

import java.util.List;

public class FlashcardRepository {
    private FlashcardDao flashcardDao;
    private LiveData<List<FlashcardEntity>> allRandomFlashcards;

    public FlashcardRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        flashcardDao = db.flashcardDao();
        allRandomFlashcards = flashcardDao.getRandomUnknownFlashcards();
    }

    public LiveData<List<FlashcardEntity>> getRandomUnknownFlashcards() {
        return allRandomFlashcards;
    }

    public void updateFlashcard(FlashcardEntity flashcard) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            flashcardDao.updateFlashcard(flashcard);
        });
    }

    public void resetAllFlashcards() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            flashcardDao.resetAllFlashcards();
        });
    }
}
