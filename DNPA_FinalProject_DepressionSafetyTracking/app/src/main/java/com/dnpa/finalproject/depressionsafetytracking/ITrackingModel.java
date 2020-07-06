package com.dnpa.finalproject.depressionsafetytracking;

import android.hardware.SensorManager;

public interface ITrackingModel {
    void startReadingData();
    void stopReadingData();
    void updateSelectedSensor(SensorManager sensorManager);
    void stopSelectedSensor(SensorManager sensorManager);
}
