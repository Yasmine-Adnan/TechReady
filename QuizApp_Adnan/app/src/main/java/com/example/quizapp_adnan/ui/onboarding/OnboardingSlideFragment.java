package com.example.quizapp_adnan.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.widget.LinearLayout;
import com.example.quizapp_adnan.R;

public class OnboardingSlideFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private int position;

    public static OnboardingSlideFragment newInstance(int position) {
        OnboardingSlideFragment fragment = new OnboardingSlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_slide, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDescription = view.findViewById(R.id.tvDescription);
        TextView tvEmoji = view.findViewById(R.id.tvEmoji);
        View dot1 = view.findViewById(R.id.dot1);
        View dot2 = view.findViewById(R.id.dot2);
        View dot3 = view.findViewById(R.id.dot3);

        switch (position) {
            case 0:
                tvTitle.setText("Bienvenue sur\nTechReady");
                tvDescription.setText("L'app premium pour réussir vos entretiens techniques.");
                tvEmoji.setText("🎓");
                setActiveDot(dot1, view);
                break;
            case 1:
                tvTitle.setText("Maîtrisez les\ncompétences");
                tvDescription.setText("Challenges de code, simulations d'entretien, et retours d'experts.");
                tvEmoji.setText("🧠");
                setActiveDot(dot2, view);
                break;
            case 2:
                tvTitle.setText("Décrochez le job");
                tvDescription.setText("Rejoignez la communauté et boostez votre carrière");
                tvEmoji.setText("🏆");
                setActiveDot(dot3, view);
                break;
        }
    }

    private void setActiveDot(View activeDot, View parentView) {
        // Convert dp to px
        int size10dp = (int) (10 * getResources().getDisplayMetrics().density);
        
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) activeDot.getLayoutParams();
        params.width = size10dp;
        params.height = size10dp;
        activeDot.setLayoutParams(params);
        activeDot.setBackgroundResource(R.drawable.shape_dot_active);
    }
}
