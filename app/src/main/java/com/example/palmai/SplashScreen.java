package com.example.palmai;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class SplashScreen extends AppCompatActivity {

    private ImageView logoImage;
    private ProgressBar progressBar2;
    private Animation logoAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        logoImage = findViewById(R.id.logoImage);
        progressBar2 = findViewById(R.id.progressBar2);
        logoAnimation = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        logoImage.setAnimation(logoAnimation);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this , MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(runnable,3000);
    }
}