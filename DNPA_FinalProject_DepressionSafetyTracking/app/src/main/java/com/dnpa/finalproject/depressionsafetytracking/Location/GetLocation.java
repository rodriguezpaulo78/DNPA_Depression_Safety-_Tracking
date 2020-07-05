package com.dnpa.finalproject.depressionsafetytracking.Location;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dnpa.finalproject.depressionsafetytracking.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class GetLocation extends AppCompatActivity implements View.OnClickListener{
    private  int MY_PERMISSIONS_REQUEST_READ_CONTACTS;
    private FusedLocationProviderClient fusedLocationClient;
    DatabaseReference mDatabase;
    private Button goMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mDatabase = FirebaseDatabase.getInstance().getReference();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        uploadLastLocation();

        goMap = findViewById(R.id.button1);
        goMap.setOnClickListener(this);

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.e("seconds remaining: ",""+ millisUntilFinished / 1000);
            }

            public void onFinish() {
                Toast.makeText(GetLocation.this,"Puntos actualizados.", Toast.LENGTH_SHORT).show();
                uploadLastLocation();
            }
        }.start();
    }

    //falta pedir permisos a la aplicacion o dispositivo
    private void uploadLastLocation() {

        // Here, thisActivity is the current activity
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);

            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            Log.e("Latitud:"+location.getLatitude(), "Longitud"+location.getLongitude());
                            Map<String,Object> latlong = new HashMap<>();
                            latlong.put("latitud", location.getLatitude());
                            latlong.put("longitud", location.getLongitude());
                            mDatabase.child("usuarios").push().setValue(latlong);
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button1 :
                Intent intent = new Intent(GetLocation.this,MapsActivity.class);
                startActivity(intent);
                break;
        }
    }


}
