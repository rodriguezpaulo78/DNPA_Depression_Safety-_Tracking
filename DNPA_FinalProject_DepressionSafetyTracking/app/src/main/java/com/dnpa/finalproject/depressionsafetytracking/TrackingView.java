package com.dnpa.finalproject.depressionsafetytracking;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class TrackingView extends AppCompatActivity implements ITrackingView{

    private ITrackingPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new TrackingPresenter(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);
    }

    public void startTracking(){

    }

    @Override
    public void showData() {

    }
}
