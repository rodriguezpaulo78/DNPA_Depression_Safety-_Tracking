package com.dnpa.finalproject.depressionsafetytracking.AudioRecord;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dnpa.finalproject.depressionsafetytracking.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.sourceforge.lame.lowlevel.LameEncoder;
import net.sourceforge.lame.mp3.Lame;
import net.sourceforge.lame.mp3.MPEGMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecordAudioActivity extends AppCompatActivity {

    private static final String TAG = "VoiceRecord";
    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS_IN = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_CHANNELS_OUT = AudioFormat.CHANNEL_OUT_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    // Initialize minimum buffer size in bytes.
    private int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING);

    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private Button btnStart, btnStop, btnPlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_activity);
        btnStart= findViewById(R.id.btnStart);
        btnStop= findViewById(R.id.btnStop);
        btnPlay= findViewById(R.id.btnPlay);

        btnStop.setEnabled(false);
        btnPlay.setEnabled(false);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(), "Recording Audio", Toast.LENGTH_LONG).show();
                    btnStop.setEnabled(true);
                    btnPlay.setEnabled(false);
                    startRecording();
                } catch (Exception e) {
                    Log.d(TAG, "Error in Start Recording ");
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(), "Stopping Recording", Toast.LENGTH_LONG).show();
                    btnStop.setEnabled(false);
                    btnPlay.setEnabled(true);
                    stopRecording();
                } catch (Exception e) {
                    Log.d(TAG, "Error in Stop Recording");
                }
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(), "Playing Recording", Toast.LENGTH_LONG).show();
                    playingRecording("/sdcard/PORFIN.pcm");
                } catch (Exception e) {
                    // make something
                }
            }
        });

        //PERMISSIONS HANDLING
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
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

    // onClick of backbutton finishes the activity.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startRecording() {
        Log.d(TAG, "Starting Recording");
        if( bufferSize == AudioRecord.ERROR_BAD_VALUE)
            Log.e( TAG, "Bad Value for \"bufferSize\", recording parameters are not supported by the hardware");
        if( bufferSize == AudioRecord.ERROR )
            Log.e( TAG, "Bad Value for \"bufferSize\", implementation was unable to query the hardware for its output properties");

        Log.e( TAG, "\"bufferSize\"="+bufferSize);

        // Initialize Audio Recorder.
        recorder = new AudioRecord(AUDIO_SOURCE, RECORDER_SAMPLERATE, RECORDER_CHANNELS_IN, RECORDER_AUDIO_ENCODING, bufferSize);
        // Starts recording from the AudioRecord instance.
        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                //Writing data into a file
                writeAudioDataToFile();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void writeAudioDataToFile() {
        //Write the output audio in byte
        String filePath = "/sdcard/PORFIN.pcm";
        byte saudioBuffer[] = new byte[bufferSize];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format
            recorder.read(saudioBuffer, 0, bufferSize);
            //Escribe l objeto a mp3

            try {
                //  writes the data to file from buffer stores the voice buffer
                os.write(saudioBuffer, 0, bufferSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopRecording() throws IOException {
        Log.d(TAG, "Stopping Recording");
        //  stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    private void playingRecording(String filePath) throws IOException{
        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath==null)
            return;

        //Reading the file..
        File file = new File(filePath); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
        byte[] byteData = new byte[(int) file.length()];
        Log.d(TAG, "laptm1" + (int) file.length()+"");


        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            in.read( byteData );
            encodePcmToMp3(byteData);
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Set and push to audio track..
        int intSize = android.media.AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING);
        Log.d(TAG, "laptm2" + intSize+"");

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING, intSize, AudioTrack.MODE_STREAM);
        if (at!=null) {
            at.play();
            // Write the byte array to the track
            at.write(byteData, 0, byteData.length);
            at.stop();

            at.release();
        }
        else
            Log.d(TAG, "audio track is not initialised ");

    }

    public void encodePcmToMp3(byte[] pcm) {
//        LameEncoder encoder = new LameEncoder(new javax.sound.sampled.AudioFormat(44100.0f, 16, 2, true, false), 256, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, false);

        //fast  gmm2  LameEncoder encoder = new LameEncoder(new javax.sound.sampled.AudioFormat(88200.0f, 16, 2, true, false), 256, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, false);
        Log.d(TAG, "laptm3" + (int) pcm.length+"");
        LameEncoder encoder = new LameEncoder(new javax.sound.sampled.AudioFormat(44100.0f, 16, 2, true, false), 256, MPEGMode.STEREO, Lame.QUALITY_HIGHEST, false);

        ByteArrayOutputStream mp3 = new ByteArrayOutputStream();
        byte[] buffer = new byte[encoder.getPCMBufferSize()];

        int bytesToTransfer = Math.min(buffer.length, pcm.length);
        int bytesWritten;
        int currentPcmPosition = 0;

        while (0 < (bytesWritten = encoder.encodeBuffer(pcm, currentPcmPosition, bytesToTransfer, buffer))) {
            currentPcmPosition += bytesToTransfer;
            bytesToTransfer = Math.min(buffer.length, pcm.length - currentPcmPosition);
            Log.e("logmessage", "current position: " + currentPcmPosition);
            mp3.write(buffer, 0, bytesWritten);
        }

        encoder.close();

        File file = new File("/sdcard/wawa.mp3");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("logmessage", "cannot create file");
            }

            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream("/sdcard/wawa.mp3");
                stream.write(mp3.toByteArray());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //   return mp3.toByteArray();
    }

}
