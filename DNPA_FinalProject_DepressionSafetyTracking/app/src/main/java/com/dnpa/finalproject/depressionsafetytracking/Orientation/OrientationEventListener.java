package com.dnpa.finalproject.depressionsafetytracking.Orientation;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.dnpa.finalproject.depressionsafetytracking.ErrorProcessing.FilteringData;
import com.dnpa.finalproject.depressionsafetytracking.Presenter.ITrackingPresenter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

//Esta clase se encargará del manejo de la lectura de orientación  (acelerómetro)
// por parte del usuario separada de la lógica principal de monitoreo
public class OrientationEventListener implements SensorEventListener {
    private static final String TAG = "Model";
    private ITrackingPresenter presenter;
    String user;
    private float[] accelerationValues;
    private float[] magneticValues;
    String ZValue, YValue, XValue;
    private boolean isFaceUp;
    String orientation=null;

    //FIREBASE
    DatabaseReference mDatabase;
    Map<String,Object> dbValues = new HashMap<>();

    private FilteringData filter;


    public OrientationEventListener(ITrackingPresenter presenter, String user){
        this.presenter = presenter;
        this.user = user;
        filter=new FilteringData();
    }

    //Manejo de lectura de datos del Sensor
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

        //APLICANDO FILTRO PASO ALTO
        orientationValues = filter.highPass(orientationValues[0], orientationValues[1], orientationValues[2],0.8f);

        SensorManager.getOrientation(rotationMatrix, orientationValues);

        double azimuth = Math.toDegrees(orientationValues[0]);
        double pitch = Math.toDegrees(orientationValues[1]);
        double roll = Math.toDegrees(orientationValues[2]);
        ZValue = String.valueOf(azimuth);
        XValue = String.valueOf(pitch);
        YValue = String.valueOf(roll);

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

    //Escribe los datos en la interfaz principal y en Firebase
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

    //Método para saber si la pantalla está hacia arriba
    private void onFaceUp() {
        if (!isFaceUp) {
            orientation=("Face up");
            isFaceUp = true;
        }
    }

    //Método para saber si la pantalla está hacia abajo
    private void onFaceDown() {
        if (isFaceUp) {
            orientation=("Face down");
            isFaceUp = false;
        }
    }
}
