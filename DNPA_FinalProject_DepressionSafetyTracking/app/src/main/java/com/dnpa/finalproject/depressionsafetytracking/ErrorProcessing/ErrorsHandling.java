package com.dnpa.finalproject.depressionsafetytracking.ErrorProcessing;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import com.dnpa.finalproject.depressionsafetytracking.View.TrackingView;

public class ErrorsHandling {

    //PERMISSIONS HANDLING
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    Context context;
    AlertDialog alert = null;

    public ErrorsHandling(Context c){
        context =c;
    }

    //Verifica si la aplicacion tiene permisos
    public void checkPermissions(){
        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, PERMISSION_ALL);
        }
    }

    //¿Se tienen los permisos?
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //Alerta por si el GPS no esta activado
    public void alertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final Intent i1 = new Intent (android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo para realizar el monitoreo?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        context.startActivity(i1);
                        //startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }

    //Revisa que GPS este activado en el dispositivo para que lea correctamente datos
    public boolean checkIfLocationOpened() {
        String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        Log.e("", "Provider contains=> "+provider);
        if (provider.contains("gps") || provider.contains("network")){
            return true;
        }
        return false;
    }
}
