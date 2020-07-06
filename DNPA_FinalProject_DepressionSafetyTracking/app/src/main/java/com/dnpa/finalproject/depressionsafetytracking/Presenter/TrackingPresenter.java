package com.dnpa.finalproject.depressionsafetytracking;

import android.hardware.SensorManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;

public class TrackingPresenter implements ITrackingPresenter {

    private ITrackingView view;
    private ITrackingModel model;

    public TrackingPresenter(ITrackingView view) {
        this.view = view;
        model = new TrackingModel(this);
    }


    @Override
    public void showData(String x, String y, String z, String orientation) {
        if(view!=null){
            view.showData( x,  y,  z,  orientation);
        }
    }

    @Override
    public void startReadingData() {
        if(view!=null){
            model.startReadingData();
        }
    }

    @Override
    public void stopReadingData() {
        if(view!=null){
            model.stopReadingData();
        }
    }

    @Override
    public void updateSelectedSensor(SensorManager sensorManager) {
        if(view!=null) {
            model.updateSelectedSensor(sensorManager);
        }
    }

    @Override
    public void stopSelectedSensor(SensorManager sensorManager) {
        if(view!=null) {
            model.stopSelectedSensor(sensorManager);
        }
    }

    @Override
    public void uploadLastLocation(AppCompatActivity act, FusedLocationProviderClient fusedLocationClient) {
        if(view!=null) {
            model.uploadLastLocation(act, fusedLocationClient);
        }
    }
}
