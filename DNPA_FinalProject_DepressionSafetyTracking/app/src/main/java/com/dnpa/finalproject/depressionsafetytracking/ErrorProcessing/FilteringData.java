package com.dnpa.finalproject.depressionsafetytracking.ErrorProcessing;

public class FilteringData {
    Float x,y,z;
    private float[] gravity = new float[3];;
    //private static final float ALPHA = 0.8f;
    private float ALPHA;

    public FilteringData(){
    }

    /**
     * MÉTODO encargado del filtrado de datos usando un filtro de paso alto
     */
    public float[] highPass(float x, float y, float z, float alpha) {
        float[] filteredValues = new float[3];

        gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
        gravity[1] = alpha * gravity[1] + (1 - alpha) * y;
        gravity[2] = alpha * gravity[2] + (1 - alpha) * z;
        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];

        return filteredValues;
    }
}
