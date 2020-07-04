package com.dnpa.finalproject.depressionsafetytracking;

public class TrackingModel implements ITrackingModel {

    private ITrackingPresenter presenter;

    public TrackingModel(ITrackingPresenter presenter){
        this.presenter = presenter;

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
