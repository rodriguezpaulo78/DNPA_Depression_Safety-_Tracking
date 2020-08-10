package com.dnpa.finalproject.depressionsafetytracking.Location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class LocationReceiver extends BroadcastReceiver {

    public LocationReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("BroadcastReceiver_ACTION")){
            Toast.makeText(context, "Puntos de ubicación actualizados", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(context, "Error in Broadcast", Toast.LENGTH_LONG).show();
        }
    }

}
