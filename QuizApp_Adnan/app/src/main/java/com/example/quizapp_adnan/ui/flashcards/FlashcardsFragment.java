package com.example.quizapp_adnan.ui.flashcards;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.data.local.FlashcardEntity;

import java.util.List;

public class FlashcardsFragment extends Fragment {

    private FlashcardsViewModel viewModel;
    private View cardFront, cardBack, cardContainer, emptyStateContainer;
    private View swipeActionsLayout;

    /** tvCategory est maintenant le compteur "3/10 — 4 maîtrisées" */
    private TextView tvCategory, tvQuestion, tvAnswer;
    private TextView btnNext, btnMastered;
    private Button btnReset;

    private AnimatorSet frontAnim, backAnim;
    private boolean isFront = true;

    private List<FlashcardEntity> currentList;
    private int currentIndex = 0;
    private int masteredCount = 0;

    // Chips
    private TextView chipArchitecture, chipAlgorithms, chipDataStructures,
            chipSystemDesign, chipPOO, chipGit, chipSQL, chipSpringBoot;
    private TextView selectedChip;

    // Swipe gesture
    private float touchStartX = 0f;
    private static final int SWIPE_THRESHOLD = 120;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flashcards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FlashcardsViewModel.class);

        // Vues principales
        tvCategory        = view.findViewById(R.id.tvCategory);
        tvQuestion        = view.findViewById(R.id.tvQuestion);
        tvAnswer          = view.findViewById(R.id.tvAnswer);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        cardFront         = view.findViewById(R.id.cardFront);
        cardBack          = view.findViewById(R.id.cardBack);
        cardContainer     = view.findViewById(R.id.cardContainer);
        swipeActionsLayout = view.findViewById(R.id.swipeActionsLayout);
        btnNext           = view.findViewById(R.id.btnNext);
        btnMastered       = view.findViewById(R.id.btnMastered);
        btnReset          = view.findViewById(R.id.btnReset);

        // Chips
        chipArchitecture  = view.findViewById(R.id.chipArchitecture);
        chipAlgorithms    = view.findViewById(R.id.chipAlgorithms);
        chipDataStructures= view.findViewById(R.id.chipDataStructures);
        chipSystemDesign  = view.findViewById(R.id.chipSystemDesign);
        chipPOO           = view.findViewById(R.id.chipPOO);
        chipGit           = view.findViewById(R.id.chipGit);
        chipSQL           = view.findViewById(R.id.chipSQL);
        chipSpringBoot    = view.findViewById(R.id.chipSpringBoot);
        selectedChip      = chipArchitecture; // sélectionnée par défaut

        setupAnimations();
        setupChipListeners();
        setupObservers();
        setupListeners();
    }

    // ─────────────────────────────────────────────────
    // Animations 3D flip (ObjectAnimator)
    // ─────────────────────────────────────────────────
    private void setupAnimations() {
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);

        try {
            frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(), R.animator.front_animator);
            backAnim  = (AnimatorSet) AnimatorInflater.loadAnimator(requireContext(), R.animator.back_animator);
        } catch (Exception e) {
            frontAnim = null;
            backAnim  = null;
        }
    }

    private void flipCard() {
        if (frontAnim == null || backAnim == null) {
            // Fallback simple
            if (isFront) {
                cardFront.setAlpha(0f);
                cardBack.setAlpha(1f);
            } else {
                cardFront.setAlpha(1f);
                cardBack.setAlpha(0f);
            }
            isFront = !isFront;
            return;
        }

        if (isFront) {
            frontAnim.setTarget(cardFront);
            backAnim.setTarget(cardBack);
        } else {
            frontAnim.setTarget(cardBack);
            backAnim.setTarget(cardFront);
        }
        frontAnim.start();
        backAnim.start();
        isFront = !isFront;
    }

    private void resetCardState() {
        if (!isFront) {
            if (frontAnim != null && backAnim != null) {
                frontAnim.setTarget(cardBack);
                backAnim.setTarget(cardFront);
                frontAnim.start();
                backAnim.start();
            } else {
                cardFront.setAlpha(1f);
                cardBack.setAlpha(0f);
            }
            isFront = true;
        }
    }

    // ─────────────────────────────────────────────────
    // Chips — sélection de catégorie
    // ─────────────────────────────────────────────────
    private void setupChipListeners() {
        View.OnClickListener chipClick = v -> selectChip((TextView) v);
        chipArchitecture.setOnClickListener(chipClick);
        chipAlgorithms.setOnClickListener(chipClick);
        chipDataStructures.setOnClickListener(chipClick);
        chipSystemDesign.setOnClickListener(chipClick);
        chipPOO.setOnClickListener(chipClick);
        chipGit.setOnClickListener(chipClick);
        chipSQL.setOnClickListener(chipClick);
        chipSpringBoot.setOnClickListener(chipClick);
    }

    private void selectChip(TextView chip) {
        // Désélectionner l'ancien chip
        if (selectedChip != null) {
            selectedChip.setBackground(requireContext().getDrawable(R.drawable.bg_chip_normal));
            selectedChip.setTextColor(0xFF2D2D3A);
        }
        // Sélectionner le nouveau
        chip.setBackground(requireContext().getDrawable(R.drawable.bg_chip_selected));
        chip.setTextColor(0xFFFFFFFF);
        selectedChip = chip;
    }

    // ─────────────────────────────────────────────────
    // Observers Room DB
    // ─────────────────────────────────────────────────
    private void setupObservers() {
        viewModel.getFlashcards().observe(getViewLifecycleOwner(), flashcards -> {
            currentList  = flashcards;
            currentIndex = 0;
            masteredCount = 0;
            displayCurrentCard();
        });
    }

    private void displayCurrentCard() {
        if (currentList == null || currentList.isEmpty()) {
            cardContainer.setVisibility(View.GONE);
            swipeActionsLayout.setVisibility(View.GONE);
            tvCategory.setVisibility(View.GONE);
            emptyStateContainer.setVisibility(View.VISIBLE);
            return;
        }

        cardContainer.setVisibility(View.VISIBLE);
        swipeActionsLayout.setVisibility(View.VISIBLE);
        tvCategory.setVisibility(View.VISIBLE);
        emptyStateContainer.setVisibility(View.GONE);

        if (currentIndex >= currentList.size()) {
            currentIndex = 0;
        }

        FlashcardEntity card = currentList.get(currentIndex);
        tvQuestion.setText(card.getQuestion());
        tvAnswer.setText(card.getAnswer());
        updateCounter();
        resetCardState();
    }

    private void updateCounter() {
        int total = currentList != null ? currentList.size() : 0;
        int displayed = currentIndex + 1;
        if (displayed > total) displayed = total;
        tvCategory.setText(displayed + "/" + total + " — " + masteredCount + " maîtrisées");
    }

    // ─────────────────────────────────────────────────
    // Listeners — tap pour flip + swipe pour action
    // ─────────────────────────────────────────────────
    private void setupListeners() {
        // Tap sur la carte → flip
        cardContainer.setOnClickListener(v -> flipCard());

        // Swipe sur la carte
        cardContainer.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchStartX = event.getX();
                    return false; // laisse aussi passer le click

                case MotionEvent.ACTION_UP:
                    float deltaX = event.getX() - touchStartX;
                    if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                        if (deltaX < 0) {
                            swipeLeft(); // À revoir
                        } else {
                            swipeRight(); // Maîtrisé
                        }
                        return true;
                    }
                    return false;
            }
            return false;
        });

        // Bouton "← À revoir 🔄"
        btnNext.setOnClickListener(v -> swipeLeft());

        // Bouton "Maîtrisé ✅ →"
        btnMastered.setOnClickListener(v -> swipeRight());

        // Reset (état vide)
        btnReset.setOnClickListener(v -> {
            masteredCount = 0;
            viewModel.resetFlashcards();
        });
    }

    /** Swipe gauche = à revoir — passe à la carte suivante sans marquer */
    private void swipeLeft() {
        if (currentList == null || currentList.isEmpty()) return;
        animateCardOut(-1, () -> {
            currentIndex++;
            displayCurrentCard();
            animateCardIn();
        });
    }

    /** Swipe droite = maîtrisé — marque et passe à la suivante */
    private void swipeRight() {
        if (currentList == null || currentList.isEmpty()) return;
        FlashcardEntity card = currentList.get(currentIndex);
        animateCardOut(1, () -> {
            viewModel.markAsKnown(card);
            masteredCount++;
            currentIndex++;
            displayCurrentCard();
            animateCardIn();
        });
    }

    private void animateCardOut(int direction, Runnable onEnd) {
        float targetX = direction * cardContainer.getWidth() * 1.3f;
        float targetRotation = direction * 18f;
        cardContainer.animate()
                .translationX(targetX)
                .rotation(targetRotation)
                .alpha(0f)
                .setDuration(280)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(onEnd)
                .start();
    }

    private void animateCardIn() {
        cardContainer.setTranslationX(0f);
        cardContainer.setRotation(0f);
        cardContainer.setAlpha(0f);
        cardContainer.animate()
                .translationX(0f)
                .rotation(0f)
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }
}
