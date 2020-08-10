package com.dnpa.finalproject.depressionsafetytracking.Model;

import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;

//Interfaz del Modelo
public interface ITrackingModel {
    void sendUserData(String x);    //Guarda el usuario a registrar data
    void updateSelectedSensor(SensorManager sensorManager);     //Inicia lectura de datos del sensor
    void stopSelectedSensor(SensorManager sensorManager);       //Detiene lectura de datos del sensor
    void uploadLastLocation(AppCompatActivity act, FusedLocationProviderClient fusedLocationClient); //Sube la ultima ubicación obtenida a la BD
}
