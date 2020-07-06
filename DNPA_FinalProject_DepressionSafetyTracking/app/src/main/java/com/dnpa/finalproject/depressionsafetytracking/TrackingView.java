package com.dnpa.finalproject.depressionsafetytracking;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.database.DatabaseReference;

public class TrackingView extends AppCompatActivity implements ITrackingView{

    private ITrackingPresenter presenter;
    DatabaseReference mDatabase;

    private Button startButton, stopButton;
    private Button mapButton, movementButton;

    //LOCATION HANDLING
    private FusedLocationProviderClient fusedLocationClient;

    //ORIENTATION HANDLING
    private TextView orientationValue;
    private TextView sensorXValue;
    private TextView sensorYValue;
    private TextView sensorZValue;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new TrackingPresenter(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);

        // Keep the screen on so that changes in orientation can be easily observed
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get a reference to the sensor service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationValue = (TextView) findViewById(R.id.orientationValue);
        sensorXValue = (TextView) findViewById(R.id.sensorXValue);
        sensorYValue = (TextView) findViewById(R.id.sensorYValue);
        sensorZValue = (TextView) findViewById(R.id.sensorZValue);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister updates from sensors
        sensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    public void showData(String x, String y, String z, String orientation) {
        //Log.d("Mensajes de entrada : ",x+y+z+""+orientation);
        sensorXValue.setText(x);
        sensorYValue.setText(y);
        sensorZValue.setText(z);
        orientationValue.setText(orientation);
    }

    public void startTracking(View view) {
        ToggleButton toggleButton = (ToggleButton)view;
        if (toggleButton.isChecked()) {
            presenter.startReadingData();
            presenter.updateSelectedSensor(sensorManager);
        } else {
            presenter.stopReadingData();
            presenter.stopSelectedSensor(sensorManager);
        }
    }

}
