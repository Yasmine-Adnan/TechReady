package com.example.quizapp_adnan.ui.interview;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.quizapp_adnan.R;
import com.example.quizapp_adnan.ui.MainActivity;
import com.example.quizapp_adnan.ui.result.ResultActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class VocalModeActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_CODE = 100;

    private VocalModeViewModel viewModel;
    private SpeechRecognizer speechRecognizer;

    private TextView tvProgress, tvQuestion, tvTranscription;
    private TextView tvScore, tvFeedback, tvPointsForts, tvAAmeliorer;
    private FloatingActionButton fabMic;
    private MaterialButton bValidate, bNext;
    private ProgressBar pbLoading;
    private View scrollFeedback;
    
    private String currentTranscription = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocal_mode);

        initViews();
        setupSpeechRecognizer();

        viewModel = new ViewModelProvider(this).get(VocalModeViewModel.class);

        viewModel.getCurrentQuestion().observe(this, question -> {
            if (question != null) {
                tvQuestion.setText(question.getQuestion());
                resetUIForNewQuestion();
            }
        });

        viewModel.getQuestionProgress().observe(this, progress -> {
            if (progress != null) {
                tvProgress.setText("Question " + progress + "/3");
            }
        });

        viewModel.getIsLoadingAi().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                pbLoading.setVisibility(View.VISIBLE);
                bValidate.setEnabled(false);
                fabMic.setEnabled(false);
            } else {
                pbLoading.setVisibility(View.GONE);
                bValidate.setEnabled(true);
            }
        });

        viewModel.getAiFeedback().observe(this, response -> {
            if (response != null) {
                // Show feedback
                scrollFeedback.setVisibility(View.VISIBLE);
                bNext.setVisibility(View.VISIBLE);
                bValidate.setVisibility(View.GONE);
                fabMic.setVisibility(View.GONE);
                tvTranscription.setVisibility(View.GONE);
                
                tvScore.setText("Score : " + response.getScore() + "/10");
                tvFeedback.setText(response.getFeedback());
                tvPointsForts.setText(response.getPointsForts());
                tvAAmeliorer.setText(response.getAAmeliorer());
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getIsFinished().observe(this, finished -> {
            if (finished != null && finished) {
                Toast.makeText(this, "Session Vocale terminée !", Toast.LENGTH_SHORT).show();
                // Redirection vers ResultActivity (ou MainActivity pour le moment)
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        bValidate.setOnClickListener(v -> {
            if (currentTranscription.trim().isEmpty()) {
                Toast.makeText(this, "Veuillez dicter une réponse d'abord.", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.validateAnswer(currentTranscription);
        });

        bNext.setOnClickListener(v -> {
            viewModel.nextQuestion();
        });

        checkPermission();
    }

    private void initViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvTranscription = findViewById(R.id.tvTranscription);
        fabMic = findViewById(R.id.fabMic);
        bValidate = findViewById(R.id.bValidate);
        bNext = findViewById(R.id.bNext);
        pbLoading = findViewById(R.id.pbLoading);
        scrollFeedback = findViewById(R.id.scrollFeedback);

        tvScore = findViewById(R.id.tvScore);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvPointsForts = findViewById(R.id.tvPointsForts);
        tvAAmeliorer = findViewById(R.id.tvAAmeliorer);
    }

    private void resetUIForNewQuestion() {
        currentTranscription = "";
        tvTranscription.setText("Maintiens le micro et commence à parler...");
        tvTranscription.setVisibility(View.VISIBLE);
        fabMic.setVisibility(View.VISIBLE);
        fabMic.setEnabled(true);
        bValidate.setVisibility(View.GONE);
        scrollFeedback.setVisibility(View.GONE);
        bNext.setVisibility(View.GONE);
    }

    private void setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    tvTranscription.setText("Écoute en cours... Parlez maintenant.");
                }
                @Override public void onBeginningOfSpeech() {}
                @Override public void onRmsChanged(float rmsdB) {}
                @Override public void onBufferReceived(byte[] buffer) {}
                @Override public void onEndOfSpeech() {
                    tvTranscription.setText("Analyse de la voix...");
                }
                @Override public void onError(int error) {
                    String message;
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO: message = "Erreur audio"; break;
                        case SpeechRecognizer.ERROR_CLIENT: message = "Erreur client"; break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: message = "Permissions insuffisantes"; break;
                        case SpeechRecognizer.ERROR_NETWORK: message = "Erreur réseau"; break;
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: message = "Délai réseau dépassé"; break;
                        case SpeechRecognizer.ERROR_NO_MATCH: message = "Aucune correspondance trouvée (Parlez plus fort)"; break;
                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: message = "Le service est occupé"; break;
                        case SpeechRecognizer.ERROR_SERVER: message = "Erreur serveur"; break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: message = "Aucune parole détectée"; break;
                        default: message = "Erreur inconnue (" + error + ")"; break;
                    }
                    Toast.makeText(VocalModeActivity.this, message, Toast.LENGTH_SHORT).show();
                    fabMic.setAlpha(1.0f);
                    if (currentTranscription.isEmpty()) {
                        tvTranscription.setText("Maintiens le micro et commence à parler...");
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (data != null && !data.isEmpty()) {
                        String recognizedText = data.get(0);
                        // Append to current if they pause and speak again, but for simplicity we overwrite or append.
                        // Overwriting is safer for simple holds.
                        currentTranscription = recognizedText;
                        tvTranscription.setText(currentTranscription);
                        bValidate.setVisibility(View.VISIBLE);
                    }
                }
                @Override public void onPartialResults(Bundle partialResults) {}
                @Override public void onEvent(int eventType, Bundle params) {}
            });

            fabMic.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    fabMic.setAlpha(0.5f);
                    speechRecognizer.startListening(speechRecognizerIntent);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    fabMic.setAlpha(1.0f);
                    speechRecognizer.stopListening();
                }
                return true;
            });
        } else {
            Toast.makeText(this, "Reconnaissance vocale non disponible sur cet appareil", Toast.LENGTH_LONG).show();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Micro autorisé", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission micro requise pour ce mode", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
