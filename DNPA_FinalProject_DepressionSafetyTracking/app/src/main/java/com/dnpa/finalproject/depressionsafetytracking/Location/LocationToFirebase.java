package com.dnpa.finalproject.depressionsafetytracking.Location;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

public class LocationToFirebase {
    private String user;
    private int index;
    private DatabaseReference mDatabase;    //DBObject
    private ArrayList<Marker> tmpRealTimeMarkers = new ArrayList<>();
    private ArrayList<Marker> realTimeMarkers = new ArrayList<>();

    public LocationToFirebase(String user, int index){
        this.user = user;
        this.index = index;
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    //Guarda los datos de ubicación obtenidos en Firebase
    public void saveToFirebase(final Context context, final GoogleMap mMap){
        //Direeccionar a la bd, cada vez que cambien los valores poner puntos en el mapa
        mDatabase.child(user.substring(0,index)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Se remueve los marcadores anteriores para actualizar a los nuevos
                for(Marker marker:realTimeMarkers){
                    marker.remove();
                }

                //Recorre todos los puntos guardados en la base de datos para dibujarlos en el mapa
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    MapsPojo mp = snapshot.getValue(MapsPojo.class);
                    Double latitud = mp.getLatitud();
                    Double longitud = mp.getLongitud();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(latitud,longitud));
                    tmpRealTimeMarkers.add(mMap.addMarker(markerOptions));
                }
                realTimeMarkers.clear();
                realTimeMarkers.addAll(tmpRealTimeMarkers);

                // checking if polylineList is Empty or not
                if (!realTimeMarkers.isEmpty()) {
                    //Auto Zoom to MAP MARKERS
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : tmpRealTimeMarkers) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();
                    int padding = 0; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    //mMap.moveCamera(cu);      //movement
                    mMap.animateCamera(cu);     //animation
                }else{
                    Toast.makeText(context, "No hay puntos de ubicacion guardados\nRegistra tus datos primero", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
