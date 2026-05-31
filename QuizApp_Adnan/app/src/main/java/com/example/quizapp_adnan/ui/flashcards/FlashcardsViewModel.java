package com.example.quizapp_adnan.ui.flashcards;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.quizapp_adnan.data.local.FlashcardEntity;
import com.example.quizapp_adnan.data.repository.FlashcardRepository;

import java.util.List;

public class FlashcardsViewModel extends AndroidViewModel {
    private FlashcardRepository repository;
    private LiveData<List<FlashcardEntity>> flashcards;

    public FlashcardsViewModel(@NonNull Application application) {
        super(application);
        repository = new FlashcardRepository(application);
        flashcards = repository.getRandomUnknownFlashcards();
    }

    public LiveData<List<FlashcardEntity>> getFlashcards() {
        return flashcards;
    }

    public void markAsKnown(FlashcardEntity flashcard) {
        flashcard.setKnown(true);
        repository.updateFlashcard(flashcard);
    }

    public void resetFlashcards() {
        repository.resetAllFlashcards();
    }
}
