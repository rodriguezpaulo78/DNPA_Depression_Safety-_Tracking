package com.dnpa.finalproject.depressionsafetytracking.Location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class LocationReceiver extends BroadcastReceiver {


    public LocationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.dnpa.finalproject.depressionsafetytracking.SOME_ACTION")){
            Toast.makeText(context, "LASTLOCATION RECEIVED", Toast.LENGTH_LONG).show();

        }else {

            Toast.makeText(context, "NOTHING", Toast.LENGTH_LONG).show();

        }
    }

}
