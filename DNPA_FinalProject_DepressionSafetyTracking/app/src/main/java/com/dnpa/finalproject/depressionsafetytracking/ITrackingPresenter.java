package com.dnpa.finalproject.depressionsafetytracking;

import android.hardware.SensorManager;

public interface ITrackingPresenter {
    void showData(String x, String y, String z, String orientation);                //Muestra los datos de orientacion XYZ
    void startReadingData();
    void stopReadingData();
    void updateSelectedSensor(SensorManager sensorManager);
    void stopSelectedSensor(SensorManager sensorManager);

}
