package com.dnpa.finalproject.depressionsafetytracking;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingModel implements ITrackingModel, SensorEventListener {

    private ITrackingPresenter presenter;

    //LOCATION HANDLING
    DatabaseReference mDatabase;

    //LOCATION HANDLING
    private static final String TAG = "DetermineOrientationActivity";
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;

    private float[] accelerationValues;
    private float[] magneticValues;
    private boolean isFaceUp;

    //LOCATION HANDLING
    String x=null,y=null,z=null,orientation=null;
    private boolean readingAccelerationData = false;

    // Initialize variables for programming
    // Initialize LOG_TAG to call in logcat to track data collected
    // The live streaming data from both the volume sensing and motion detecting can be seen in the logcat
    private static final String LOG_TAG = "DataPointCollection";
    // Initializes the MediaRecorder as mRecorder for volume tracking
    private MediaRecorder mRecorder = null;
    // Creates list of maximum amplitude values collected
    // Would be used to send data to server
    public static List<String> MaxValue = new ArrayList();
    // Initializes the SensorManager and Sensor for motion sensing

    public TrackingModel(ITrackingPresenter presenter){
        this.presenter = presenter;
    }

    @Override
    public void startReadingData() {
        if (!readingAccelerationData) {
            readingAccelerationData = true;
            startRecording();
        }
    }

    @Override
    public void stopReadingData() {
        if (readingAccelerationData) {
            readingAccelerationData = false;
            stopRecording();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:

                x = (String.valueOf(event.values[0]));
                y=(String.valueOf(event.values[1]));
                z=(String.valueOf(event.values[2]));

                break;

            case Sensor.TYPE_ACCELEROMETER:
                accelerationValues = event.values.clone();
                rotationMatrix = generateRotationMatrix();
                if (rotationMatrix != null) {
                    determineOrientation(rotationMatrix);
                }
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                magneticValues = event.values.clone();
                rotationMatrix = generateRotationMatrix();
                if (rotationMatrix != null) {
                    determineOrientation(rotationMatrix);
                }
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                rotationMatrix = new float[16];
                SensorManager.getRotationMatrixFromVector(rotationMatrix,
                        event.values);
                determineOrientation(rotationMatrix);
                break;
        }

        //MICROPHONE
        // Constantly calls getAmplitude function
        double amp = getAmplitude();
        String max = String.valueOf(amp);
        MaxValue.add(max);
        // Shows constant stream of maximum amplitude values in logcat
        Log.e(LOG_TAG, "Max Amplitude " +amp);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, String.format("Accuracy for sensor %s = %d", sensor.getName(), accuracy));
    }

    /**
     * Generates a rotation matrix using the member data stored in
     * accelerationValues and magneticValues.
     */
    @SuppressLint("LongLogTag")
    private float[] generateRotationMatrix() {
        float[] rotationMatrix = null;

        if (accelerationValues != null && magneticValues != null) {
            rotationMatrix = new float[16];
            boolean rotationMatrixGenerated;
            rotationMatrixGenerated =
                    SensorManager.getRotationMatrix(rotationMatrix,
                            null, accelerationValues, magneticValues);
            if (!rotationMatrixGenerated) {
                Log.w(TAG, "Failed to generate rotation matrix");
                rotationMatrix = null;
            }
        }
        return rotationMatrix;
    }

    /**
     * Uses the last read accelerometer and gravity values to determine if the
     * device is face up or face down.
     *
     * @param rotationMatrix The rotation matrix to use if the orientation calculation
     */
    @SuppressLint("LongLogTag")
    private void determineOrientation(float[] rotationMatrix) {
        float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        double azimuth = Math.toDegrees(orientationValues[0]);
        double pitch = Math.toDegrees(orientationValues[1]);
        double roll = Math.toDegrees(orientationValues[2]);
        String ZValue = String.valueOf(azimuth);
        String XValue = String.valueOf(pitch);
        String YValue = String.valueOf(roll);


        x=(String.valueOf(azimuth));
        y=(String.valueOf(pitch));
        z=(String.valueOf(roll));

        Log.d(TAG, "Valores = (" + ZValue + ", " + XValue + ", " +YValue +")");


        //Para ver si esta de cabeza o de frente
        if (pitch <= 10) {
            if (Math.abs(roll) >= 170) {
                onFaceDown();
                presenter.showData(XValue,YValue,ZValue,orientation);
            } else if (Math.abs(roll) <= 10) {
                onFaceUp();
                presenter.showData(XValue,YValue,ZValue,orientation);
            }
        }
    }

    /**
     * Handler for device being face up.
     */
    private void onFaceUp() {
        if (!isFaceUp) {
            orientation=("Face up");
            isFaceUp = true;
        }
    }

    /**
     * Handler for device being face down.
     */
    private void onFaceDown() {
        if (isFaceUp) {
            orientation=("Face down");
            isFaceUp = false;
        }
    }

    /**
     * Updates the views for when the selected sensor is changed
     */
    public void updateSelectedSensor(SensorManager sensorManager) {
        // Clear any current registrations
        sensorManager.unregisterListener(this);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), RATE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), RATE);
    }

    /**
     * Updates the views for when the selected sensor is changed
     */
    public void stopSelectedSensor(SensorManager sensorManager) {
        // Clear any current registrations
        sensorManager.unregisterListener(this);
        presenter.showData("","","","");
    }

    @Override
    public void uploadLastLocation(AppCompatActivity act, FusedLocationProviderClient fusedLocationClient) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(act, new OnSuccessListener<Location>() {
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

                        /*
                        new CountDownTimer(5000, 1000) {
                            public void onTick(long millisUntilFinished) {
                                Log.e("seconds remaining: ",""+ millisUntilFinished / 1000);
                            }
                            public void onFinish() {
                                Toast.makeText(act,"Puntos actualizados.", Toast.LENGTH_SHORT).show();
                            }
                        }.start();
                         */
                    }
                });
    }

    /**
     * Updates the views for when the selected sensor is changed
     */
    public void startRecording(){
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


    /**
     * Updates the views for when the selected sensor is changed
     */
    public void stopRecording(){
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

}
