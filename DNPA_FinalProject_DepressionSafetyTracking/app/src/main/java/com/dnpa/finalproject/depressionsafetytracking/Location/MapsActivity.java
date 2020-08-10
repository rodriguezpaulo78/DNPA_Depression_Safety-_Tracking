package com.dnpa.finalproject.depressionsafetytracking.Location;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dnpa.finalproject.depressionsafetytracking.AudioRecording.AudioToFirebase;
import com.dnpa.finalproject.depressionsafetytracking.R;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;                 //mapObject

    private LocationToFirebase locationSaver;

    public String user;
    public int index;

    //Dialog about
    Dialog myDialog;
    ImageView closeAboutImg;
    Button btnAccept;
    TextView titleTv, messageTv;

    Button mBackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        user =getIntent().getStringExtra("USER");
        index = getIntent().getIntExtra("INDEX",0);

        locationSaver = new LocationToFirebase(user, index);

        myDialog = new Dialog(this);
        mBackBtn = (Button)findViewById(R.id.back);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //show message
        showMessage();

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationSaver.saveToFirebase(this, mMap);
    }

    //Mostrar CUSTOM DIALOG BOX
    public void showMessage(){
        myDialog.setContentView(R.layout.maps_message);
        closeAboutImg = (ImageView) myDialog.findViewById(R.id.closeAbout);
        btnAccept = (Button) myDialog.findViewById(R.id.btnAceptar);
        titleTv = (TextView) myDialog.findViewById(R.id.titleAbout);
        messageTv = (TextView) myDialog.findViewById(R.id.messageAbout);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        closeAboutImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }
}
