/*
 * Copyright 2012 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dnpa.finalproject.depressionsafetytracking.Movement;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.XYPlot;
import com.dnpa.finalproject.depressionsafetytracking.R;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

//Clase encargada del activity que muestra los datos del usuario en tiempo real por medio de un gráfico(movimiento)
public class ShowMovementActivity extends AppCompatActivity {
    private static final String TAG = "ShowMovementActivity";
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;
    private static final String USE_HIGH_PASS_FILTER_PREFERENCE_KEY =
            "USE_HIGH_PASS_FILTER_PREFERENCE_KEY";
    private static final String SELECTED_SENSOR_TYPE_PREFERENCE_KEY =
            "SELECTED_SENSOR_TYPE_PREFERENCE_KEY";
    
    private SensorManager sensorManager;
    private RadioGroup sensorSelector;
    private int selectedSensorType;
    private boolean readingAccelerationData;
    private SharedPreferences preferences;
    private AccelerationEventListener accelerometerListener;
    private XYPlot xyPlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.movement_activity);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        sensorSelector = (RadioGroup)findViewById(R.id.sensorSelector);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        readingAccelerationData = false;
        preferences = getPreferences(MODE_PRIVATE);

        selectedSensorType =
                preferences.getInt(SELECTED_SENSOR_TYPE_PREFERENCE_KEY, Sensor.TYPE_ACCELEROMETER);

        ((RadioButton)findViewById(R.id.accelerometer)).setChecked(true);
        
        xyPlot = (XYPlot)findViewById(R.id.XYPlot);
        xyPlot.setDomainLabel("Elapsed Time (ms)");
        xyPlot.setRangeLabel("Acceleration (m/sec^2)");
        xyPlot.setBorderPaint(null);
        xyPlot.disableAllMarkup();
        xyPlot.setRangeBoundaries(-10, 10, BoundaryMode.FIXED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopReadingAccelerationData();
    }

    public void onSensorSelectorClick(View view) {
        selectedSensorType = Sensor.TYPE_ACCELEROMETER;
    }

    public void onReadAccelerationDataToggleButtonClicked(View view) {
        ToggleButton toggleButton = (ToggleButton)view;
        if (toggleButton.isChecked()) {
            startReadingAccelerationData();
        } else {
            stopReadingAccelerationData();
        }
    }

    @SuppressLint("LongLogTag")
    private void startReadingAccelerationData() {
        if (!readingAccelerationData) {
            // Limpiar
            xyPlot.clear();
            xyPlot.redraw();
            
            // Deshabilitar componentes cuando se monitorea
            for (int i = 0; i < sensorSelector.getChildCount(); i++) {
                sensorSelector.getChildAt(i).setEnabled(false);
            }

            xyPlot.setTitle("Sensor.TYPE_ACCELEROMETER");
                accelerometerListener = new AccelerationEventListener(xyPlot, true);

            sensorManager.registerListener(accelerometerListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), RATE);
            
            readingAccelerationData = true;
            Log.d(TAG, "Started reading acceleration data");
        }
    }

    @SuppressLint("LongLogTag")
    private void stopReadingAccelerationData() {
        if (readingAccelerationData) {
            // Rehabilitar
            for (int i = 0; i < sensorSelector.getChildCount(); i++) {
                sensorSelector.getChildAt(i).setEnabled(true);
            }

            sensorManager.unregisterListener(accelerometerListener);
            readingAccelerationData = false;
            Log.d(TAG, "Stopped reading acceleration data");
        }
    }

}
