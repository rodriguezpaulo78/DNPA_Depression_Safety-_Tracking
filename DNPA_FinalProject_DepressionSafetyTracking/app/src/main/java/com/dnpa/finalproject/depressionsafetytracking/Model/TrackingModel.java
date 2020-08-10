package com.dnpa.finalproject.depressionsafetytracking.Model;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.dnpa.finalproject.depressionsafetytracking.Orientation.OrientationEventListener;
import com.dnpa.finalproject.depressionsafetytracking.Presenter.ITrackingPresenter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class TrackingModel implements ITrackingModel {

    private ITrackingPresenter presenter;

    //LOCATION HANDLING
    DatabaseReference mDatabase;
    Map<String,Object> dbValues = new HashMap<>();

    //ORIENTATION HANDLING
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;
    public static String user;
    private OrientationEventListener orientationListener;

    public TrackingModel(ITrackingPresenter presenter){
        this.presenter = presenter;
    }

    @Override
    public void sendUserData(String a) {
            user = a;
    }

    /**
     * Actualiza los valores del sensor al iniciar monitoreo
     */
    public void updateSelectedSensor(SensorManager sensorManager) {
        // Limpiar algun registro realizado anteriormente
        orientationListener = new OrientationEventListener(presenter, user);
        sensorManager.unregisterListener(orientationListener);
        sensorManager.registerListener(orientationListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), RATE);
        sensorManager.registerListener(orientationListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), RATE);
    }

    /**
     * Actualiza los valores al detener el monitoreo
     */
    public void stopSelectedSensor(SensorManager sensorManager) {
        // Limpiar algun registro realizado anteriormente
        sensorManager.unregisterListener(orientationListener);

        //Llama al método del presentador para actualizar los valores
        presenter.showData("","","","");
    }

    @SuppressLint("MissingPermission")
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
                            //Guarda los valores en la BD
                            dbValues.put("latitud", location.getLatitude());
                            dbValues.put("longitud", location.getLongitude());
                            mDatabase.child(user).push().setValue(dbValues);
                            //mDatabase.removeValue();
                        }
                    }
                });
    }
}
