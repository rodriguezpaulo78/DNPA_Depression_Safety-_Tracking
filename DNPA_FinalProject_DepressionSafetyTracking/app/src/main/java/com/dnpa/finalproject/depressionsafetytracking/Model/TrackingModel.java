package com.dnpa.finalproject.depressionsafetytracking.Model;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.dnpa.finalproject.depressionsafetytracking.Presenter.ITrackingPresenter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class TrackingModel implements ITrackingModel, SensorEventListener {

    private ITrackingPresenter presenter;

    //LOCATION HANDLING
    DatabaseReference mDatabase;
    Map<String,Object> dbValues = new HashMap<>();

    //ORIENTATION HANDLING
    private static final String TAG = "Model";
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;
    private float[] accelerationValues;
    private float[] magneticValues;
    private boolean isFaceUp;
    String x=null,y=null,z=null,orientation=null;
    private boolean readingAccelerationData = false;

    public static String user;
    String ZValue, YValue, XValue;

    public TrackingModel(ITrackingPresenter presenter){
        this.presenter = presenter;
    }

    @Override
    public void sendUserData(String a) {
            user = a;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix;

        switch (event.sensor.getType()) {
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
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, String.format("Accuracy for sensor %s = %d", sensor.getName(), accuracy));
    }

    /**
     * Genera una matriz de rotación a partir de los valores de los sensores
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
     * Usa los valores de los sensores para determinar la orientación del dispositivo
     * asi como también mostrar tales valores en la interfaz  y guardarlos en la BD
     * @param rotationMatrix
     */
    private void determineOrientation(float[] rotationMatrix) {
        float[] orientationValues = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientationValues);

        double azimuth = Math.toDegrees(orientationValues[0]);
        double pitch = Math.toDegrees(orientationValues[1]);
        double roll = Math.toDegrees(orientationValues[2]);
        ZValue = String.valueOf(azimuth);
        XValue = String.valueOf(pitch);
        YValue = String.valueOf(roll);

        x=(String.valueOf(azimuth));
        y=(String.valueOf(pitch));
        z=(String.valueOf(roll));

        Map<String,Object> xyzValues = new HashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Para ver si esta de cabeza o de frente
        if (pitch <= 10) {
            if (Math.abs(roll) >= 170) {
                onFaceDown();
                writeData();
            } else if (Math.abs(roll) <= 10) {
                onFaceUp();
                writeData( );
            }
        }
    }

    @SuppressLint("LongLogTag")
    private void writeData(){
        Log.d(TAG, "Valores = (" + ZValue + ", " + XValue + ", " +YValue +")");
        //Guarda los valores en la BD
        dbValues.put("x (orient)", XValue);
        dbValues.put("y (orient)", YValue);
        dbValues.put("z (orient)", ZValue);
        mDatabase.child(user).push().setValue(dbValues);
        //mDatabase.removeValue(); //método para eliminar si hay muchos datos en BD

        //Llama al método del presentador para actualizar los valores
        presenter.showData(XValue,YValue,ZValue,orientation);
    }

    private void onFaceUp() {
        if (!isFaceUp) {
            orientation=("Face up");
            isFaceUp = true;
        }
    }

    private void onFaceDown() {
        if (isFaceUp) {
            orientation=("Face down");
            isFaceUp = false;
        }
    }

    /**
     * Actualiza los valores del sensor al iniciar monitoreo
     */
    public void updateSelectedSensor(SensorManager sensorManager) {
        // Limpiar algun registro realizado anteriormente
        sensorManager.unregisterListener(this);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), RATE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), RATE);
    }

    /**
     * Actualiza los valores al detener el monitoreo
     */
    public void stopSelectedSensor(SensorManager sensorManager) {
        // Limpiar algun registro realizado anteriormente
        sensorManager.unregisterListener(this);

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
