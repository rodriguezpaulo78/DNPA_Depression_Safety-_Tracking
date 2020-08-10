package com.dnpa.finalproject.depressionsafetytracking.AudioRecording;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dnpa.finalproject.depressionsafetytracking.Login.LoginActivity;
import com.dnpa.finalproject.depressionsafetytracking.Login.RegisterActivity;
import com.dnpa.finalproject.depressionsafetytracking.R;

public class RecordAudioActivity extends AppCompatActivity {

    private static final String TAG = "Voice Record Activity";
    private Button btnStart, btnStop, btnPlay, btnSave, mBackBtn;
    private AudioProcessing audioHandler;

    public String user;
    public int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Audio Recording");

        user =getIntent().getStringExtra("USER");
        index = getIntent().getIntExtra("INDEX",0);

        //Objeto AudioProcessing encargado de operaciones de grabación
        audioHandler =  new AudioProcessing("/sdcard/DSTRecord", user, index);

        mBackBtn = (Button)findViewById(R.id.back);
        btnStart= findViewById(R.id.btnStart);
        btnStop= findViewById(R.id.btnStop);
        btnPlay= findViewById(R.id.btnPlay);
        btnSave= findViewById(R.id.btnSend);
        btnStop.setEnabled(false);
        btnPlay.setEnabled(false);
        btnSave.setEnabled(false);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(), "Recording Audio", Toast.LENGTH_SHORT).show();
                    btnStop.setEnabled(true);
                    btnPlay.setEnabled(false);
                    btnSave.setEnabled(false);
                    audioHandler.startRecording();
                } catch (Exception e) {
                    Log.d(TAG, "Error in Start Recording ");
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(), "Stopping Recording", Toast.LENGTH_SHORT).show();
                    btnStop.setEnabled(false);
                    btnPlay.setEnabled(true);
                    btnSave.setEnabled(true);
                    audioHandler.stopRecording();
                } catch (Exception e) {
                    Log.d(TAG, "Error in Stop Recording");
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(), "Playing Recording", Toast.LENGTH_SHORT).show();
                    audioHandler.playingRecording();
                } catch (Exception e) {
                    Log.d(TAG, "Error in Playing Recording");
                }
            }
        });

        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(RecordAudioActivity.this)
                        .setTitle("Confirmación")
                        .setMessage("Esta seguro de que desea guardar esta grabacion?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    Toast.makeText(getApplicationContext(), "Record saved in Firebase", Toast.LENGTH_SHORT).show();
                                    audioHandler.savingRecording();
                                } catch (Exception e) {
                                    Log.d(TAG, "Error in Saving Recording");
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();

            }
        });
    }

    //onClick of Backbutton finishes the activity.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
