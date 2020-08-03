package com.dnpa.finalproject.depressionsafetytracking.Splash;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.dnpa.finalproject.depressionsafetytracking.R;
import com.dnpa.finalproject.depressionsafetytracking.View.TrackingView;

public class SplashScreenView extends AppCompatActivity {

    //Duracion SPLASH
    private final int SPLASH_DURATION = 2500;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_activity);

        new Handler().postDelayed(new Runnable(){
            public void run(){
                Intent intent = new Intent(SplashScreenView.this, TrackingView.class);
                startActivity(intent);
                finish();
            };
        }, SPLASH_DURATION);
    }
}
