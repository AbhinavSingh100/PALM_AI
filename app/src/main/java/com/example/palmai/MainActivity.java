package com.example.palmai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private EditText requestText;
    private Button generateBtn;
    private TextView responseText;
    private ProgressBar progressBar;
    private Button copyText;
    private ImageButton imageButton;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private String prompt = "Write a short poem on planes.";
    private String API_KEY = "AIzaSyAzWNFaly5q7CBs5MyT3mKYBfaj3gkKF0w";
    private String url = "https://generativelanguage.googleapis.com/v1beta3/models/text-bison-001:generateText?key="+API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestText = findViewById(R.id.requestText);
        generateBtn = findViewById(R.id.generateBtn);
        responseText = findViewById(R.id.responseText);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        copyText = findViewById(R.id.copyText);
        imageButton = findViewById(R.id.imageButton);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.UK);

                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        // Language data is missing or not supported
                        Toast.makeText(MainActivity.this, "Language not supported.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Initialization failed
                    Toast.makeText(MainActivity.this, "TextToSpeech failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        generateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prompt = "You are an ai assistant. Answer the question in the double quote as an ai assistant in a polite tone:\""+requestText.getText().toString()+"\"";
                if(prompt.isEmpty()) {
                    prompt = "Write a short poem on planes.";
                }
                palmResponse(prompt);
            }
        });

        copyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", responseText.getText().toString());
                if (clipboardManager != null) {
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(getApplicationContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSpeaking)
                {
                    textToSpeech.stop();
                    isSpeaking = false;
                    imageButton.setImageDrawable(getDrawable(android.R.drawable.ic_btn_speak_now));
                }
                else {
                    isSpeaking = true;
                    speak();
                    imageButton.setImageDrawable(getDrawable(android.R.drawable.ic_media_pause));
                }

            }
        });

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isSpeaking = false;
                        imageButton.setImageDrawable(getDrawable(android.R.drawable.ic_btn_speak_now));
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {

            }
        });
    }

    private void speak() {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageID");
        textToSpeech.speak(responseText.getText().toString(), TextToSpeech.QUEUE_FLUSH, params);
    }

    private void palmResponse(String prompt) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JSONObject request = new JSONObject();
        JSONObject requestText = new JSONObject();
        try {
            requestText.put("text",prompt);
            request.put("prompt",requestText);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url , request,  new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String stringOutput = response.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getString("output");

                    responseText.setText(stringOutput);
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseText.setText("Error 69");
                progressBar.setVisibility(View.GONE);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Content-Type", "application/json");
                return mapHeader;
            }
        };

        int intTimeoutPeriod = 60000; //60 seconds
        RetryPolicy retryPolicy = new DefaultRetryPolicy(intTimeoutPeriod,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

        jsonObjectRequest.setRetryPolicy(retryPolicy);
        queue.add(jsonObjectRequest);

    }
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

}