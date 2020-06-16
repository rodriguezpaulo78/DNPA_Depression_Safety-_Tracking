package com.dnpa.depressionSafetyTracking;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import android.media.MediaPlayer;

/**
 * Determines whether the device is face up or face down and gives a audio
 * notification (via TTS) when the face-up/face-down orientation changes.
 *
 * @author Adam Stroud &#60;<a href="mailto:adam.stroud@gmail.com">adam.stroud@gmail.com</a>&#62;
 */
public class ObtainingValues extends AppCompatActivity implements SensorEventListener {
    private static final String TAG = "ObtainingValues";
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

    private float mHighPassX1, mHighPassX2, mHighPassX3;
    private float mLastX, mLastY, mLastZ;
    private float a1 = 0.7f;
    private float a2 = 0.85f;
    private float a3 = 0.95f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);

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
                sensorXLabel.setText(R.string.xAxisLabel);
                sensorXValue.setText(String.valueOf(event.values[0]));

                sensorYLabel.setText(R.string.yAxisLabel);
                sensorYValue.setText(String.valueOf(event.values[1]));

                sensorZLabel.setText(R.string.zAxisLabel);
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
                Log.w(TAG, getString(R.string.rotationMatrixGenFailureMessage));
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

        //FILTRADO
        mHighPassX1 = highPass1(orientationValues[1], mLastX, mHighPassX1);
        mHighPassX2 = highPass2(orientationValues[1], mLastX, mHighPassX2);
        mHighPassX3 = highPass3(orientationValues[1], mLastX, mHighPassX3);
        mLastX = orientationValues[1];

        Log.d(TAG, ","+orientationValues[1]+"," + mHighPassX1+","+mHighPassX2+","+mHighPassX3);

        //END FILTRADO

        sensorXLabel.setText(R.string.azimuthLabel);
        sensorXValue.setText(String.valueOf(azimuth));

        sensorYLabel.setText(R.string.pitchLabel);
        sensorYValue.setText(String.valueOf(pitch));

        sensorZLabel.setText(R.string.rollLabel);
        sensorZValue.setText(String.valueOf(roll));

        Log.d(TAG, "Valores = (" + ZValue + ", " + XValue + ", " +YValue +")");

        sensorYLabel.setVisibility(View.VISIBLE);
        sensorYValue.setVisibility(View.VISIBLE);
        sensorZLabel.setVisibility(View.VISIBLE);
        sensorZValue.setVisibility(View.VISIBLE);

        MediaPlayer mediaPlayer1 = MediaPlayer.create(this, R.raw.alarma_de_pie);
        MediaPlayer mediaPlayer2 = MediaPlayer.create(this, R.raw.alarma_mesa);

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
                mediaPlayer1.start();
            }
        }

        //Para ver si esta en el bolsillo
        if (pitch >= -80 && pitch <= -60) {
            Log.d(TAG, "EL DISPOSITIVO ESTÁ EN EL BOLSILLO DE UNA PERSONA QUE SE ENCUENTRA DE PIE");
            mediaPlayer2.start();
        }
    }

    // simple high-pass filter
    float highPass1(float current, float last, float filtered)
    {
        return a1 * (filtered + current - last);
    }

    float highPass2(float current, float last, float filtered)
    {
        return a2 * (filtered + current - last);
    }

    float highPass3(float current, float last, float filtered)
    {
        return a3 * (filtered + current - last);
    }

    /**
     * Handler for device being face up.
     */
    private void onFaceUp() {
        if (!isFaceUp) {
            orientationValue.setText(R.string.faceUpText);
            isFaceUp = true;
        }
    }

    /**
     * Handler for device being face down.
     */
    private void onFaceDown() {
        if (isFaceUp) {
            orientationValue.setText(R.string.faceDownText);
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

