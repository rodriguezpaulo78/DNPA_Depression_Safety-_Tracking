package com.dnpa.finalproject.depressionsafetytracking.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dnpa.finalproject.depressionsafetytracking.Location.LocationReceiver;
import com.dnpa.finalproject.depressionsafetytracking.Location.MapsActivity;
import com.dnpa.finalproject.depressionsafetytracking.Movement.ShowMovementActivity;
import com.dnpa.finalproject.depressionsafetytracking.Presenter.ITrackingPresenter;
import com.dnpa.finalproject.depressionsafetytracking.Presenter.TrackingPresenter;
import com.dnpa.finalproject.depressionsafetytracking.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class TrackingView extends AppCompatActivity implements ITrackingView, View.OnClickListener{

    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS;
    private ITrackingPresenter presenter;

    private Button mapButton, movementButton;

    //LOCATION HANDLING
    private FusedLocationProviderClient fusedLocationClient;
    LocationReceiver receiver;
    IntentFilter intentFilter;
    AlertDialog alert = null;

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

        //LOCATION HANDLING
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapButton = findViewById(R.id.lastLocationsButton);
        mapButton.setOnClickListener(this);
        receiver = new LocationReceiver();
        intentFilter = new IntentFilter("com.dnpa.finalproject.depressionsafetytracking.SOME_ACTION");

        //ORIENTATION HANDLING
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Obtener referencia del sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationValue = (TextView) findViewById(R.id.orientationValue);
        sensorXValue = (TextView) findViewById(R.id.sensorXValue);
        sensorYValue = (TextView) findViewById(R.id.sensorYValue);
        sensorZValue = (TextView) findViewById(R.id.sensorZValue);

        //MOVEMENT HANDLING
        movementButton = findViewById(R.id.movementButton);
        movementButton.setOnClickListener(this);

        //PERMISSIONS HANDLING
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void showData(String x, String y, String z, String orientation) {
        //Log.d("Mensajes de entrada : "x+y+z+""+orientation);
        sensorXValue.setText(x);
        sensorYValue.setText(y);
        sensorZValue.setText(z);
        orientationValue.setText(orientation);
    }

    //Revisa que GPS este activado en el dispositivo para que lea correctamente datos
    private boolean checkIfLocationOpened() {
        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.e("", "Provider contains=> "+provider);
        if (provider.contains("gps") || provider.contains("network")){
            return true;
        }
        return false;
    }

    //Llama a los métodos del presentador para iniciar monitoreo
    public void startTracking(View view) {
        ToggleButton toggleButton = (ToggleButton)view;

        if (checkIfLocationOpened()){
            if (toggleButton.isChecked()) {
                presenter.startReadingData();
                presenter.updateSelectedSensor(sensorManager);
                presenter.uploadLastLocation(this, fusedLocationClient);
            } else {
                presenter.stopReadingData();
                presenter.stopSelectedSensor(sensorManager);
            }
        }else{
            Log.e("", "LLAMANDO METODO PARA ACTIVAR GPS ");
            alertNoGps();

        }
    }

    @Override
    //Manejo de opciones de usuario a través de Intents
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lastLocationsButton :
                //Envia la acción al Broadcast Receiver
                Intent intent = new Intent("com.dnpa.finalproject.depressionsafetytracking.SOME_ACTION");
                sendBroadcast(intent);
                Intent mapIntent = new Intent(TrackingView.this,MapsActivity.class);
                startActivity(mapIntent);
                break;
            case R.id.movementButton :
                Intent movementIntent = new Intent(TrackingView.this, ShowMovementActivity.class);
                startActivity(movementIntent);
                break;
        }
    }

    //¿Se tienen los permisos?
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //Alerta por si el GPS no esta activado
    private void alertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo para realizar el monitoreo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }
}
