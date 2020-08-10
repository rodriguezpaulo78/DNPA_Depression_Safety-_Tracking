package com.dnpa.finalproject.depressionsafetytracking.Presenter;

import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;

//Presenter Interface
public interface ITrackingPresenter {

    void showData(String x, String y, String z, String orientation);    //Muestra los datos obtenidos del modelo
    void startReadingData(String x);
    void stopReadingData();
    void updateSelectedSensor(SensorManager sensorManager);
    void stopSelectedSensor(SensorManager sensorManager);
    void uploadLastLocation(AppCompatActivity act, FusedLocationProviderClient fusedLocationClient);
}
