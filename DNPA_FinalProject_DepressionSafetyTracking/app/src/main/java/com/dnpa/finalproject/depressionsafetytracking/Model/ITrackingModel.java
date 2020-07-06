package com.dnpa.finalproject.depressionsafetytracking.Model;

import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;

public interface ITrackingModel {

    //ORIENTATION
    void startReadingData();
    void stopReadingData();
    void updateSelectedSensor(SensorManager sensorManager);
    void stopSelectedSensor(SensorManager sensorManager);
    void uploadLastLocation(AppCompatActivity act, FusedLocationProviderClient fusedLocationClient);
}
