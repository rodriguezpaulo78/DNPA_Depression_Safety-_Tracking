package com.dnpa.finalproject.depressionsafetytracking.Model;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.dnpa.finalproject.depressionsafetytracking.Presenter.ITrackingPresenter;
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
    Map<String,Object> dbValues = new HashMap<>();

    //ORIENTATION HANDLING
    private static final String TAG = "DetermineOrientationActivity";
    private static final int RATE = SensorManager.SENSOR_DELAY_NORMAL;
    private float[] accelerationValues;
    private float[] magneticValues;
    private boolean isFaceUp;
    String x=null,y=null,z=null,orientation=null;
    private boolean readingAccelerationData = false;

    // Valores inicializados para lectura de datos (microfono)
    private static final String LOG_TAG = "DataPointCollection";
    private MediaRecorder mRecorder = null;
    public static List<String> MaxValue = new ArrayList();

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
        // Llama a la funcion getAmplitude() constantemente.
        double amp = getAmplitude();
        String max = String.valueOf(amp);
        MaxValue.add(max);
        Log.e(LOG_TAG, "Max Amplitude " +amp);

        //Se registra el valor obtenido en la base de datos en tiempo real.
        mDatabase = FirebaseDatabase.getInstance().getReference();
        dbValues.put("amplitud(mic)", max);
        mDatabase.child("usuarios").push().setValue(dbValues);
    }

    @SuppressLint("LongLogTag")
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

        Map<String,Object> xyzValues = new HashMap<>();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Para ver si esta de cabeza o de frente
        if (pitch <= 10) {
            if (Math.abs(roll) >= 170) {
                onFaceDown();

                //Guarda los valores en la BD
                dbValues.put("x (acel)", XValue);
                dbValues.put("y (acel)", YValue);
                dbValues.put("z (acel)", ZValue);
                mDatabase.child("usuarios").push().setValue(dbValues);

                //Llama al método del presentador para actualizar los valores
                presenter.showData(XValue,YValue,ZValue,orientation);
            } else if (Math.abs(roll) <= 10) {
                onFaceUp();

                //Guarda los valores en la BD
                dbValues.put("x (acel)", XValue);
                dbValues.put("y (acel)", YValue);
                dbValues.put("z (acel)", ZValue);
                mDatabase.child("usuarios").push().setValue(dbValues);

                //Llama al método del presentador para actualizar los valores
                presenter.showData(XValue,YValue,ZValue,orientation);
            }
        }
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
                            mDatabase.child("usuarios").push().setValue(dbValues);
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
     * Empieza el registro de los datos del microfono
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
            getAmplitude();
        }
        Log.e(LOG_TAG, "Start Record Called");
    }


    /**
     * Detiene el registro de los datos del microfono
     */
    public void stopRecording(){
        if (mRecorder != null){
            mRecorder.stop();
            mRecorder.reset();
            mRecorder = null;
        }
        Log.e(LOG_TAG, "Stop Record Called");
    }

    // Implementa el filtro correspondiente
    private double getAmplitude() {
        if (mRecorder != null) {
            // Shows getAmplitude function has been called in logcat
            Log.e(LOG_TAG, "getAmplitude Function Called");
            double max = 20*Math.log(mRecorder.getMaxAmplitude()/2700);
            return (max);
        } else {
            return 0;
        }
    }

    // Funcion para recolectar los datos de maxima amplitud
    public double getAmplitudeMax() {
        double amp = getAmplitude();
        String max = String.valueOf(amp);
        MaxValue.add(max);
        // Shows value of maximum amplitude in logcat
        Log.e(LOG_TAG, "Max Amplitude " +amp);
        return amp;
    }

}
