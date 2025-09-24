package com.example.tasklistmcpclient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextView speechRecognizerTextView;
    private Button recordButton;
    private Button stopRecordingButton;
    private Button sendButton;
    // --------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAndRequestPermissions();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --------------------
        speechRecognizerTextView = findViewById(R.id.speechRecognizerTextView);
        recordButton = findViewById(R.id.recordButton);
        recordButton.setOnClickListener(v -> {
            speechRecognizer.startListening(speechRecognizerIntent);
            recordButton.setEnabled(false);
            stopRecordingButton.setEnabled(true);
        });

        stopRecordingButton = findViewById(R.id.stopRecordingButton);
        stopRecordingButton.setOnClickListener(v -> {
            speechRecognizer.stopListening();
            recordButton.setEnabled(true);
            stopRecordingButton.setEnabled(false);
        });

        sendButton = findViewById(R.id.sendButton);

        stopRecordingButton.setEnabled(false);
        sendButton.setEnabled(false);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US"); // Specify language
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                speechRecognizerTextView.setText(R.string.listening_speech_recognizer_text_view_text);
            }

            @Override
            public void onBeginningOfSpeech() {
                // User started speaking
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Volume level of the input audio
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Raw audio buffer
            }

            @Override
            public void onEndOfSpeech() {
                speechRecognizerTextView.setText(R.string.processing_speech_recognizer_text_view_text);
            }

            @Override
            public void onError(int error) {
                speechRecognizerTextView.setText("Error: " + getErrorText(error));

                recordButton.setEnabled(true);
                stopRecordingButton.setEnabled(false);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    speechRecognizerTextView.setText(matches.get(0)); // Display the most confident result
                }
                recordButton.setEnabled(true);
                stopRecordingButton.setEnabled(false);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Can be used to show partial recognition results as the user speaks
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // For future use
            }
        });

        // --------------------
        speechRecognizerTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String currentText = s.toString(); // Get the actual string content

                String defaultText = getString(R.string.default_speech_recognizer_text_view_text);
                String listeningText = getString(R.string.listening_speech_recognizer_text_view_text);
                String processingText = getString(R.string.processing_speech_recognizer_text_view_text);

                if (!currentText.isEmpty()
                        && !currentText.equals(defaultText)
                        && !currentText.equals(listeningText)
                        && !currentText.equals(processingText)
                        && !currentText.startsWith("Error:")
                ) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // N/A
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // N/A
            }
        });
    }

    // --------------------

    final int RECORD_AUDIO_PERMISSION_CODE = 1;
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with speech recognition setup
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "Recognition service busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "Error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}