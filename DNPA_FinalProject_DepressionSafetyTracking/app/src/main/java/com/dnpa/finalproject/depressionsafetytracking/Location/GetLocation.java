package com.dnpa.finalproject.depressionsafetytracking.Location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dnpa.finalproject.depressionsafetytracking.Microphone.UsingMicrophone;
import com.dnpa.finalproject.depressionsafetytracking.Movement.DetermineMovementActivity;
import com.dnpa.finalproject.depressionsafetytracking.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetLocation extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    private  int MY_PERMISSIONS_REQUEST_READ_CONTACTS;
    private FusedLocationProviderClient fusedLocationClient;
    DatabaseReference mDatabase;
    private Button goMap;
    private Button btn,btn1,btn2;



    // Initialize variables for programming
    // Initialize LOG_TAG to call in logcat to track data collected
    // The live streaming data from both the volume sensing and motion detecting
    // can be seen in the logcat
    private static final String LOG_TAG = "DataPointCollection";
    // Initializes the MediaRecorder as mRecorder for volume tracking
    private MediaRecorder mRecorder = null;
    // Creates list of maximum amplitude values collected
    // Would be used to send data to server
    public static List<String> MaxValue = new ArrayList();
    // Initializes the SensorManager and Sensor for motion sensing
    private SensorManager mSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        uploadLastLocation();

        goMap = findViewById(R.id.button3);
        btn = findViewById(R.id.button4);
        goMap.setOnClickListener(this);
        btn.setOnClickListener(this);

        ////_----
        btn1 = findViewById(R.id.button1);
        btn2 = findViewById(R.id.button2);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        ////_----

        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                Log.e("seconds remaining: ",""+ millisUntilFinished / 1000);
            }
            public void onFinish() {
                Toast.makeText(GetLocation.this,"Puntos actualizados.", Toast.LENGTH_SHORT).show();
                uploadLastLocation();
            }
        }.start();
    }

    //falta pedir permisos a la aplicacion o dispositivo
    private void uploadLastLocation() {

        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.e("Latitud:"+location.getLatitude(), "Longitud"+location.getLongitude());
                            Map<String,Object> latlong = new HashMap<>();
                            latlong.put("latitud", location.getLatitude());
                            latlong.put("longitud", location.getLongitude());
                            mDatabase.child("usuarios").push().setValue(latlong);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button1 :
                // Calls start recording function on Start button (button) push
                onRestart();
                // Calls start linear accelerometer function on Start button (button) push
                onResume();
                // Notifies the user tracking has begun
                Toast.makeText(GetLocation.this, "Tracking",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.button2 :
                // Calls stop recording function on Stop button(button2) push
                onStop();
                // Calls stop linear accelerometer function on Stop button (button2) push
                onPause();
                // Notifies the user tracking has stopped
                Toast.makeText(GetLocation.this, "Tracking Stopped",
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.button3 :
                Intent intent = new Intent(GetLocation.this,MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.button4 :
                Intent intent2 = new Intent(GetLocation.this, DetermineMovementActivity.class);
                startActivity(intent2);
                break;
        }
    }

    // Start recording function
    protected void onRestart() {
        super.onRestart();
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile("/dev/null");
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
            mRecorder.start();
            // Calls getAmplitude to use getMaxAmplitude filter
            getAmplitude();
        }
        // Shows start record function has been called in logcat
        Log.e(LOG_TAG, "Start Record Called");
    }

    // Stop recording function
    protected void onStop(){
        super.onStop();
        if (mRecorder != null){
            mRecorder.stop();
            mRecorder.reset();
            mRecorder = null;
        }
        // Shows stop record function has been called in logcat
        Log.e(LOG_TAG, "Stop Record Called");
    }

    // Function to implement getMaxAmplitude filter
    private double getAmplitude() {
        if (mRecorder != null) {
            // Shows getAmplitude function has been called in logcat
            Log.e(LOG_TAG, "getAmplitude Function Called");
            // Calls getMaxAmplitude function
            // Equation is used to get the maximum amplitude in decibels
            double max = 20*Math.log(mRecorder.getMaxAmplitude()/2700);
            return (max);
        } else {
            return 0;
        }
    }

    // Function to collect maximum amplitude data
    public double getAmplitudeMax() {
        double amp = getAmplitude();
        // Converts double amp to string max
        // This MaxValue would be used to create an array to send to server
        String max = String.valueOf(amp);
        MaxValue.add(max);
        // Shows value of maximum amplitude in logcat
        Log.e(LOG_TAG, "Max Amplitude " +amp);
        return amp;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Constantly calls getAmplitude function
        double amp = getAmplitude();
        String max = String.valueOf(amp);
        MaxValue.add(max);
        // Shows constant stream of maximum amplitude values in logcat
        Log.e(LOG_TAG, "Max Amplitude " +amp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
