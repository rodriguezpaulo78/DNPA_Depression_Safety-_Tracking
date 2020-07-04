package com.dnpa.finalproject.depressionsafetytracking;

public class TrackingPresenter implements ITrackingPresenter {

    private ITrackingView view;
    private ITrackingModel model;

    public TrackingPresenter(ITrackingView view){
        this.view = view;
        model = new TrackingModel(this);
    }

    @Override
    public void showData() {

    }

    @Override
    public void detectingUbication() {

    }

    @Override
    public void detectingOrientation() {

    }

    @Override
    public void detectingMovement() {

    }

    @Override
    public void detectingMicrophone() {

    }
}
