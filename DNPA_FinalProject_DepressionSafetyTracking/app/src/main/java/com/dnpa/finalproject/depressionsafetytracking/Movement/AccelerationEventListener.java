/*
 * Copyright 2012 Greg Milette and Adam Stroud
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dnpa.finalproject.depressionsafetytracking.Movement;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

//Esta clase se encarga del manejo de la lectura de datos (del acelerometro) por parte del usuario
// separada de la lógica principal de monitoreo de movimiento
public class AccelerationEventListener implements SensorEventListener {
    private static final String TAG = "AccelerationEventListener";
    private static final float ALPHA = 0.8f;
    private static final int HIGH_PASS_MINIMUM = 10;
    private static final int MAX_SERIES_SIZE = 30;
    private static final int CHART_REFRESH = 125;

    private long startTime;
    private float[] gravity;
    private int highPassCount;
    private SimpleXYSeries xAxisSeries;
    private SimpleXYSeries yAxisSeries;
    private SimpleXYSeries zAxisSeries;
    private SimpleXYSeries accelerationSeries;
    private XYPlot xyPlot;
    private long lastChartRefresh;
    private boolean useHighPassFilter;
    private String user;
    private int i;

    //BD Firebase
    DatabaseReference mDatabase;
    Map<String,Object> dbValues = new HashMap<>();

    public AccelerationEventListener(XYPlot xyPlot, boolean useHighPassFilter, String a, int index) {
        user= a;
        i=index;

        this.xyPlot = xyPlot;
        this.useHighPassFilter = useHighPassFilter;
        
        xAxisSeries = new SimpleXYSeries("X Axis");
        yAxisSeries = new SimpleXYSeries("Y Axis");
        zAxisSeries = new SimpleXYSeries("Z Axis");
        accelerationSeries = new SimpleXYSeries("Acceleration");
        
        gravity = new float[3];
        startTime = SystemClock.uptimeMillis();
        highPassCount = 0;

        if (xyPlot != null) {
            xyPlot.addSeries(xAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.RED, null, null));
            xyPlot.addSeries(yAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.GREEN, null, null));
            xyPlot.addSeries(zAxisSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.BLUE, null, null));
            xyPlot.addSeries(accelerationSeries,
                             LineAndPointRenderer.class,
                             new LineAndPointFormatter(Color.CYAN, null, null));
        }
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onSensorChanged(SensorEvent event){
        float[] values = event.values.clone();
        Log.d(TAG, "Valores = (" + values[0] + ", " + values[1] + ", " +values[2] +")");

        //Escribe los valores obtenidos en la BD
        mDatabase = FirebaseDatabase.getInstance().getReference();
        dbValues.put("x(mov)", values[0]);
        dbValues.put("y(mov)", values[1]);
        dbValues.put("z(mov)", values[2]);
        mDatabase.child(user.substring(0,i)).push().setValue(dbValues);

        // Pasa los valores usando el filtro de paso alto
        if (useHighPassFilter) {
            values = highPass(values[0], values[1], values[2]);
        }

        if (!useHighPassFilter || (++highPassCount >= HIGH_PASS_MINIMUM)) {
            double sumOfSquares = (values[0] * values[0])
                    + (values[1] * values[1])
                    + (values[2] * values[2]);
            double acceleration = Math.sqrt(sumOfSquares);

            Log.d(TAG, "Aceleración Total = (" + acceleration + ")");

            //Grafica los valores de los sensores en tiempo real
            if (xyPlot != null) {
                long current = SystemClock.uptimeMillis();

                // Limit how much the chart gets updated
                if ((current - lastChartRefresh) >= CHART_REFRESH) {
                    long timestamp = (event.timestamp / 1000000) - startTime;
                    
                    // Plot data
                    addDataPoint(xAxisSeries, timestamp, values[0]);
                    addDataPoint(yAxisSeries, timestamp, values[1]);
                    addDataPoint(zAxisSeries, timestamp, values[2]);
                    addDataPoint(accelerationSeries, timestamp, acceleration);
                    
                    xyPlot.redraw();
                    lastChartRefresh = current;
                }
            }
        }
    }

    private void addDataPoint(SimpleXYSeries series, Number timestamp, Number value) {
        if (series.size() == MAX_SERIES_SIZE) {
            series.removeFirst();
        }
        series.addLast(timestamp, value);
    }

    /**
     * MÉTODO encargado del filtrado
     */
    private float[] highPass(float x, float y, float z) {
        float[] filteredValues = new float[3];
        
        gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * x;
        gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * y;
        gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * z;
        filteredValues[0] = x - gravity[0];
        filteredValues[1] = y - gravity[1];
        filteredValues[2] = z - gravity[2];
        
        return filteredValues;
    }

    //Método encargado de calcular la aceleración total en caso se requiera
    private double totalaceleration(float x, float y, float z) {
        double totalaceleration = Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
        return totalaceleration;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
        // no-op
    }
}
