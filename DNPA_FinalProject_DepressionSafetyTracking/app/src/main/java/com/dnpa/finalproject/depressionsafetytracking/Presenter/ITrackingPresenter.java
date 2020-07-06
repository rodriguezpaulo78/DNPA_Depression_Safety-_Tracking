package com.dnpa.finalproject.depressionsafetytracking.Presenter;

import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;

public interface ITrackingPresenter {

    //ORIENTATION
    void showData(String x, String y, String z, String orientation);
    void startReadingData();
    void stopReadingData();
    void updateSelectedSensor(SensorManager sensorManager);
    void stopSelectedSensor(SensorManager sensorManager);
    void uploadLastLocation(AppCompatActivity act, FusedLocationProviderClient fusedLocationClient);

}
