package com.dnpa.finalproject.depressionsafetytracking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dnpa.finalproject.depressionsafetytracking.Location.LocationReceiver;
import com.dnpa.finalproject.depressionsafetytracking.Location.MapsActivity;
import com.dnpa.finalproject.depressionsafetytracking.Movement.ShowMovementActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class TrackingView extends AppCompatActivity implements ITrackingView, View.OnClickListener{

    private ITrackingPresenter presenter;


    private Button mapButton, movementButton;

    //LOCATION HANDLING
    private FusedLocationProviderClient fusedLocationClient;
    LocationReceiver receiver;
    IntentFilter intentFilter;

    //ORIENTATION HANDLING
    private TextView orientationValue;
    private TextView sensorXValue;
    private TextView sensorYValue;
    private TextView sensorZValue;
    private SensorManager sensorManager;

    //MOVEMENT HANDLING

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new TrackingPresenter(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);

        //LOCATION HANDLING
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapButton = findViewById(R.id.button3);
        mapButton.setOnClickListener(this);
        receiver = new LocationReceiver();
        intentFilter = new IntentFilter("com.dnpa.finalproject.depressionsafetytracking.SOME_ACTION");

        //ORIENTATION HANDLING
        // Keep the screen on so that changes in orientation can be easily observed
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Get a reference to the sensor service
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationValue = (TextView) findViewById(R.id.orientationValue);
        sensorXValue = (TextView) findViewById(R.id.sensorXValue);
        sensorYValue = (TextView) findViewById(R.id.sensorYValue);
        sensorZValue = (TextView) findViewById(R.id.sensorZValue);

        //MOVEMENT HANDLING
        movementButton = findViewById(R.id.button4);
        movementButton.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister updates from sensors

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
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
            presenter.uploadLastLocation(this, fusedLocationClient);
        } else {
            presenter.stopReadingData();
            presenter.stopSelectedSensor(sensorManager);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button3 :
                Intent intent = new Intent("com.dnpa.finalproject.depressionsafetytracking.SOME_ACTION");
                sendBroadcast(intent);
                Intent intent2 = new Intent(TrackingView.this,MapsActivity.class);
                startActivity(intent2);
                break;
            case R.id.button4 :
                Intent intent3 = new Intent(TrackingView.this, ShowMovementActivity.class);
                startActivity(intent3);
                break;
        }
    }
}
