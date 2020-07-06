package com.dnpa.finalproject.depressionsafetytracking.Microphone;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dnpa.finalproject.depressionsafetytracking.Movement.DetermineMovementActivity;
import com.dnpa.finalproject.depressionsafetytracking.R;
import com.dnpa.finalproject.depressionsafetytracking.TrackingView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//Falta obtener permisos de microfono
public class UsingMicrophone extends AppCompatActivity implements SensorEventListener {

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);
        //initializeViews();
        // Starts linear accelerometer
        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
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

    // Registers the linear accelerometer for listening the events
    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        // Shows onResume function has been called in logcat
        Log.e(LOG_TAG, "Motion Resume Called");
    }
    // Unregisters the linear accelerometer for stop listening the events
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        // Shows onPause function has been called in logcat
        Log.e(LOG_TAG, "Motion Pause Called");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        // Reaction to Start button (button) pushed
        final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {

            @Override public void onClick (View view){
                // Calls start recording function on Start button (button) push
                onRestart();
                // Calls start linear accelerometer function on Start button (button) push
                onResume();
                // Notifies the user tracking has begun
                Toast.makeText(UsingMicrophone.this, "Tracking",
                        Toast.LENGTH_SHORT).show();
            }
        }
        );

        /*
        // Reaction to Stop button (button2) pushed
        Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Calls stop recording function on Stop button(button2) push
                onStop();
                // Calls stop linear accelerometer function on Stop button (button2) push
                onPause();
                // Notifies the user tracking has stopped
                Toast.makeText(UsingMicrophone.this, "Tracking Stopped",
                        Toast.LENGTH_SHORT).show();
            }
        });

         */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        /*
        if (id == R.id.action_settings) {
            return true;
        }

         */
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.e(LOG_TAG, "POR LA PTMR ON SENSOR CHANGED  ");
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
