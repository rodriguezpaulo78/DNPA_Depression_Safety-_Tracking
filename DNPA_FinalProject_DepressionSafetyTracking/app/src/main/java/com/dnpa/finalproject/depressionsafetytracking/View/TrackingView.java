package com.dnpa.finalproject.depressionsafetytracking.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.bumptech.glide.Glide;
import com.dnpa.finalproject.depressionsafetytracking.AudioRecording.RecordAudioActivity;
import com.dnpa.finalproject.depressionsafetytracking.ErrorProcessing.ErrorsHandling;
import com.dnpa.finalproject.depressionsafetytracking.Location.LocationReceiver;
import com.dnpa.finalproject.depressionsafetytracking.Location.MapsActivity;
import com.dnpa.finalproject.depressionsafetytracking.Movement.ShowMovementActivity;
import com.dnpa.finalproject.depressionsafetytracking.Presenter.ITrackingPresenter;
import com.dnpa.finalproject.depressionsafetytracking.Presenter.TrackingPresenter;
import com.dnpa.finalproject.depressionsafetytracking.R;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class TrackingView extends AppCompatActivity implements ITrackingView, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener{

    private ITrackingPresenter presenter;
    private int MY_PERMISSIONS_REQUEST_READ_CONTACTS;
    private final static long ACC_CHECK_INTERVAL = 3000;
    private long lastAccCheck;

    private Button mapButton, movementButton, audioButton, showButton;
    private String textToFirebase;
    private int index;

    //LOCATION HANDLING
    private FusedLocationProviderClient fusedLocationClient;
    LocationReceiver receiver;
    IntentFilter intentFilter;
    AlertDialog alert = null;

    //ORIENTATION HANDLING
    private TextView orientationLabel, orientationValue;
    private TextView sensorXLabel, sensorXValue;
    private TextView sensorYLabel, sensorYValue;
    private TextView sensorZLabel, sensorZValue;
    private SensorManager sensorManager;

    //Actualizar en Navigation Drawer
    NavigationView navigationView;
    TextView nav_user;
    TextView nav_mail;
    ImageView nav_image;
    private DrawerLayout drawer;

    //Dialog about
    Dialog myDialog;
    ImageView closeAboutImg;
    Button btnAccept;
    TextView titleTv, messageTv;

    //LOGIN
    String EmailHolder;
    //ERRROS HANDLING
    private ErrorsHandling getErrors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        presenter = new TrackingPresenter(this);
        getErrors = new ErrorsHandling(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracking_activity);

        //LOCATION HANDLING
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapButton = findViewById(R.id.lastLocationsButton);
        mapButton.setOnClickListener(this);
        receiver = new LocationReceiver();
        intentFilter = new IntentFilter("com.dnpa.finalproject.depressionsafetytracking.SOME_ACTION");

        //ORIENTATION HANDLING
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Obtener referencia del sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationValue = (TextView) findViewById(R.id.orientationValue);
        orientationLabel = (TextView) findViewById(R.id.orientationLabel);
        sensorXLabel = (TextView) findViewById(R.id.sensorXLabel);
        sensorXValue = (TextView) findViewById(R.id.sensorXValue);
        sensorYLabel = (TextView) findViewById(R.id.sensorYLabel);
        sensorYValue = (TextView) findViewById(R.id.sensorYValue);
        sensorZLabel = (TextView) findViewById(R.id.sensorZLabel);
        sensorZValue = (TextView) findViewById(R.id.sensorZValue);

        //MOVEMENT HANDLING
        movementButton = findViewById(R.id.movementButton);
        movementButton.setOnClickListener(this);

        //SHOW - AUDIO
        audioButton = findViewById(R.id.audioButton);
        audioButton.setOnClickListener(this);
        showButton = findViewById(R.id.showButton);
        showButton.setOnClickListener(this);

        getErrors.checkPermissions();

        //NAV BAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        nav_user = (TextView) hView.findViewById(R.id.name_user);
        nav_mail = (TextView) hView.findViewById(R.id.mail_user);
        nav_image = (ImageView) hView.findViewById(R.id.image_user);

        if(savedInstanceState == null){
            navigationView.setCheckedItem(R.id.nav_home);
        }
        myDialog = new Dialog(this);

        //Datos de usuario
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Glide.with(this).load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(nav_image);
            //nav_image.setImageURI(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl());
            nav_user.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            nav_mail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            textToFirebase = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            index = textToFirebase.indexOf('@');
        }

        //Mensaje de confidencialidad
        showPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    //Muestra los datos al usuario
    @Override
    public void showData(String x, String y, String z, String orientation) {
        //Log.d("Mensajes de entrada : "x+y+z+""+orientation);
        sensorXValue.setText(x);
        sensorYValue.setText(y);
        sensorZValue.setText(z);
        orientationValue.setText(orientation);
    }

    //Llama a los métodos del presentador para iniciar monitoreo
    public void startTracking(View view) {
        ToggleButton toggleButton = (ToggleButton)view;

        if (getErrors.checkIfLocationOpened()){
            if (toggleButton.isChecked()) {
                presenter.sendUserData(textToFirebase.substring(0,index));
                presenter.updateSelectedSensor(sensorManager);
                presenter.uploadLastLocation(this, fusedLocationClient);
            } else {
                presenter.stopSelectedSensor(sensorManager);
            }
        }else{
            Log.e("", "LLAMANDO METODO PARA ACTIVAR GPS ");
            toggleButton.setChecked(false);
            getErrors.alertNoGps();
        }
    }

    @Override
    //Manejo de opciones de usuario a través de Intents
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lastLocationsButton :
                //Envia la acción al Broadcast Receiver
                Intent intent = new Intent("BroadcastReceiver_ACTION");
                sendBroadcast(intent);
                Intent mapIntent = new Intent(TrackingView.this,MapsActivity.class);
                mapIntent.putExtra("USER", textToFirebase);
                mapIntent.putExtra("INDEX", index);
                startActivity(mapIntent);
                break;
            case R.id.movementButton :
                Intent movementIntent = new Intent(TrackingView.this, ShowMovementActivity.class);
                movementIntent.putExtra("USER", textToFirebase);
                movementIntent.putExtra("INDEX", index);
                startActivity(movementIntent);
                break;
            case R.id.audioButton :
                Intent audioIntent = new Intent(TrackingView.this, RecordAudioActivity.class);
                audioIntent.putExtra("USER", textToFirebase);
                audioIntent.putExtra("INDEX", index);
                startActivity(audioIntent);
                break;
            case R.id.showButton :
                hideView(v);
                break;
        }
    }

    //Funcionalidad para ocultar y mostrar datos
    public void hideView(View view) {
        ToggleButton toggleButton = (ToggleButton) view;
            if (toggleButton.isChecked()) {
                orientationLabel.setVisibility(View.INVISIBLE);
                orientationValue.setVisibility(View.INVISIBLE);
                sensorXLabel.setVisibility(View.INVISIBLE);
                sensorXValue.setVisibility(View.INVISIBLE);
                sensorYLabel.setVisibility(View.INVISIBLE);
                sensorYValue.setVisibility(View.INVISIBLE);
                sensorZLabel.setVisibility(View.INVISIBLE);
                sensorZValue.setVisibility(View.INVISIBLE);
            } else {
                orientationLabel.setVisibility(View.VISIBLE);
                orientationValue.setVisibility(View.VISIBLE);
                sensorXLabel.setVisibility(View.VISIBLE);
                sensorXValue.setVisibility(View.VISIBLE);
                sensorYLabel.setVisibility(View.VISIBLE);
                sensorYValue.setVisibility(View.VISIBLE);
                sensorZLabel.setVisibility(View.VISIBLE);
                sensorZValue.setVisibility(View.VISIBLE);
            }
    }

    //Interactua con las opciones del Navigation Bar
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_home:
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                break;
            case R.id.nav_gps:
                //Intent mapIntent = new Intent(TrackingView.this,MapsActivity.class);
                //startActivity(mapIntent);
                break;
            case R.id.nav_mov:
                //Intent movementIntent = new Intent(TrackingView.this, ShowMovementActivity.class);
                //startActivity(movementIntent);
                break;
            case R.id.nav_audio:
                //Intent audioIntent = new Intent(TrackingView.this, RecordAudioActivity.class);
                //startActivity(audioIntent);
                break;
            case R.id.nav_ajustes:
                //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AjustesFragment()).commit();
                break;
            case R.id.nav_about:
                showAbout();
                break;
            case R.id.nav_help:
                //showAbout();
                break;
            case R.id.nav_salir:
                new AlertDialog.Builder(this)
                        .setTitle("Confirmación de Salida")
                        .setMessage("Esta seguro de que desea cerrar sesion?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                signOut();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Cierra sesión
    public void signOut(){
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(TrackingView.this, "Sign out Successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    //Creacion de opciones del Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    //Manejo de opciones del Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ajustes:
                Toast.makeText(this, "Settings Menu", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.acerca:
                showAbout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Mostrar CUSTOM DIALOG BOX
    public void showAbout(){
        myDialog.setContentView(R.layout.about);
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

    //Mostrar CUSTOM DIALOG BOX
    public void showPermissions(){
        myDialog.setContentView(R.layout.permissions);
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
