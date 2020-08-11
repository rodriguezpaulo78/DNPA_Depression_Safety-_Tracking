package com.dnpa.finalproject.depressionsafetytracking.Movement;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.SystemClock;
import android.util.Log;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.LineAndPointRenderer;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.dnpa.finalproject.depressionsafetytracking.ErrorProcessing.FilteringData;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    private FilteringData filter;

    //BD Firebase
    DatabaseReference mDatabase;
    Map<String,Object> dbValues = new HashMap<>();

    public AccelerationEventListener(XYPlot xyPlot, boolean useHighPassFilter, String a, int index) {
        user= a;
        i=index;
        filter=new FilteringData();

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

    //Manejo de lectura de datos del Sensor
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
            //values = highPass(values[0], values[1], values[2]);
            values = filter.highPass(values[0], values[1], values[2],0.8f);
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

    //Adición de datos en el gráfico
    private void addDataPoint(SimpleXYSeries series, Number timestamp, Number value) {
        if (series.size() == MAX_SERIES_SIZE) {
            series.removeFirst();
        }
        series.addLast(timestamp, value);
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, String.format("Accuracy for sensor %s = %d", sensor.getName(), accuracy));
    }
}
