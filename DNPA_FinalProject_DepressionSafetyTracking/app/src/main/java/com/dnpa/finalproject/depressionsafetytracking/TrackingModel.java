package com.dnpa.finalproject.depressionsafetytracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dnpa.finalproject.depressionsafetytracking.Location.GetLocation;
import com.dnpa.finalproject.depressionsafetytracking.Location.MapsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.SENSOR_SERVICE;

public class TrackingModel implements ITrackingModel, SensorEventListener {

    private ITrackingPresenter presenter;

    DatabaseReference mDatabase;


    private static final String TAG = "DetermineOrientationActivity";
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;

    private float[] accelerationValues;
    private float[] magneticValues;
    private boolean isFaceUp;

    String x=null,y=null,z=null,orientation=null;
    private boolean readingAccelerationData = false;

    public TrackingModel(ITrackingPresenter presenter){
        this.presenter = presenter;
    }

    @Override
    public void startReadingData() {
        if (!readingAccelerationData) {
            readingAccelerationData = true;
        }
    }

    @Override
    public void stopReadingData() {
        if (readingAccelerationData) {
            readingAccelerationData = false;
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
                    }
                });
    }


}
