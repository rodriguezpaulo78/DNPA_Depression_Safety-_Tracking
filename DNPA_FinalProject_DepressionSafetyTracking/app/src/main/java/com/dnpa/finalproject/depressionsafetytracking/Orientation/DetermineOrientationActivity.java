package com.dnpa.finalproject.depressionsafetytracking.Orientation;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dnpa.finalproject.depressionsafetytracking.R;

/**
 * Determines whether the device is face up or face down and gives a audio
 * notification (via TTS) when the face-up/face-down orientation changes.
 *
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class DetermineOrientationActivity extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "DetermineOrientationActivity";
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;

    private SensorManager sensorManager;
    private float[] accelerationValues;
    private float[] magneticValues;
    private boolean isFaceUp;
    private RadioGroup sensorSelector;
    private TextView selectedSensorValue;
    private TextView orientationValue;
    private TextView sensorXLabel;
    private TextView sensorXValue;
    private TextView sensorYLabel;
    private TextView sensorYValue;
    private TextView sensorZLabel;
    private TextView sensorZValue;
    private int selectedSensorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.tracking_activity);

        // Keep the screen on so that changes in orientation can be easily
        // observed
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get a reference to the sensor service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Initialize references to the UI views that will be updated in the
        // code
        sensorSelector = (RadioGroup) findViewById(R.id.sensorSelector);
        selectedSensorValue = (TextView) findViewById(R.id.selectedSensorValue);
        orientationValue = (TextView) findViewById(R.id.orientationValue);
        sensorXLabel = (TextView) findViewById(R.id.sensorXLabel);
        sensorXValue = (TextView) findViewById(R.id.sensorXValue);
        sensorYLabel = (TextView) findViewById(R.id.sensorYLabel);
        sensorYValue = (TextView) findViewById(R.id.sensorYValue);
        sensorZLabel = (TextView) findViewById(R.id.sensorZLabel);
        sensorZValue = (TextView) findViewById(R.id.sensorZValue);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSelectedSensor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister updates from sensors
        sensorManager.unregisterListener(this);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix;

        switch (event.sensor.getType()) {
            case Sensor.TYPE_GRAVITY:
                sensorXLabel.setText("X Axis:");
                sensorXValue.setText(String.valueOf(event.values[0]));

                sensorYLabel.setText("Y Axis:");
                sensorYValue.setText(String.valueOf(event.values[1]));

                sensorZLabel.setText("Z Axis:");
                sensorZValue.setText(String.valueOf(event.values[2]));

                sensorYLabel.setVisibility(View.VISIBLE);
                sensorYValue.setVisibility(View.VISIBLE);
                sensorZLabel.setVisibility(View.VISIBLE);
                sensorZValue.setVisibility(View.VISIBLE);

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
        Log.d(TAG,
                String.format("Accuracy for sensor %s = %d",
                        sensor.getName(), accuracy));
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
                            null,
                            accelerationValues,
                            magneticValues);
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
     * @param rotationMatrix The rotation matrix to use if the orientation
     *                       calculation
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

        sensorXLabel.setText("Azimuth (Z Axis):");
        sensorXValue.setText(String.valueOf(azimuth));

        sensorYLabel.setText("Pitch (X Axis):");
        sensorYValue.setText(String.valueOf(pitch));

        sensorZLabel.setText("Roll (Y Axis):");
        sensorZValue.setText(String.valueOf(roll));

        Log.d(TAG, "Valores = (" + ZValue + ", " + XValue + ", " +YValue +")");

        sensorYLabel.setVisibility(View.VISIBLE);
        sensorYValue.setVisibility(View.VISIBLE);
        sensorZLabel.setVisibility(View.VISIBLE);
        sensorZValue.setVisibility(View.VISIBLE);

        //Para ver si esta de cabeza o de frente
        if (pitch <= 10) {
            if (Math.abs(roll) >= 170) {
                onFaceDown();
            } else if (Math.abs(roll) <= 10) {
                onFaceUp();
            }
        }

        //Para ver si esta en la mesa
        if (pitch >= -1 && pitch <= 1) {
            if (roll >= -1 && roll <= 1) {
                Log.d(TAG, "EL DISPOSITIVO ESTÁ SOBRE LA MESA");

            }
        }

        //Para ver si esta en el bolsillo
        if (pitch >= -80 && pitch <= -60) {
            Log.d(TAG, "EL DISPOSITIVO ESTÁ EN EL BOLSILLO DE UNA PERSONA QUE SE ENCUENTRA DE PIE");

        }
    }

    /**
     * Handler for device being face up.
     */
    private void onFaceUp() {
        if (!isFaceUp) {
            orientationValue.setText("Face up");
            isFaceUp = true;
        }
    }

    /**
     * Handler for device being face down.
     */
    private void onFaceDown() {
        if (isFaceUp) {
            orientationValue.setText("Face down");
            isFaceUp = false;
        }
    }

    /**
     * Updates the views for when the selected sensor is changed
     */
    private void updateSelectedSensor() {
        // Clear any current registrations
        sensorManager.unregisterListener(this);

        // Determine which radio button is currently selected and enable the
        // appropriate sensors
        selectedSensorId = sensorSelector.getCheckedRadioButtonId();
        if (selectedSensorId == R.id.accelerometerMagnetometer) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    RATE);

            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    RATE);
        } else {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    RATE);
        }

        // Update the label with the currently selected sensor
        RadioButton selectedSensorRadioButton =
                (RadioButton) findViewById(selectedSensorId);
        selectedSensorValue.setText(selectedSensorRadioButton.getText());
    }

    /**
     * Handles click event for the sensor selector.
     *
     * @param view The view that was clicked
     */
    public void onSensorSelectorClick(View view) {
        updateSelectedSensor();
    }
}

