package com.dnpa.finalproject.depressionsafetytracking;

public class MapsPojo {

    //el mismo nombre en firebase
    private double latitud;
    private double longitud;

    public MapsPojo(){

    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }
}
