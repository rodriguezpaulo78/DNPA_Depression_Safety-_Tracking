package com.dnpa.finalproject.depressionsafetytracking.Presenter;

import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;
import com.dnpa.finalproject.depressionsafetytracking.View.ITrackingView;
import com.dnpa.finalproject.depressionsafetytracking.Model.ITrackingModel;
import com.dnpa.finalproject.depressionsafetytracking.Model.TrackingModel;
import com.google.android.gms.location.FusedLocationProviderClient;

public class TrackingPresenter implements ITrackingPresenter {

    private ITrackingView view;
    private ITrackingModel model;

    public TrackingPresenter(ITrackingView view) {
        this.view = view;
        model = new TrackingModel(this);
    }

    //Llamada a métodos desarrollados en el modelo
    @Override
    public void showData(String x, String y, String z, String orientation) {
        if(view!=null){
            //Muestra los datos recibidos en la vista
            view.showData( x,  y,  z,  orientation);
        }
    }

    @Override
    public void sendUserData(String x) {
        if(view!=null){
            model.sendUserData(x);
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
