package com.dnpa.finalproject.depressionsafetytracking.AudioRecording;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioProcessing {

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

    private String filePath;
    private AudioConversion audioConverter;
    private AudioToFirebase audioSaver;

    private String user;
    private int index;

    public AudioProcessing(String filePath, String user, int index){
        this.filePath = filePath;
        this.user = user;
        this.index = index;
        audioConverter = new AudioConversion(filePath);
        audioSaver = new AudioToFirebase(filePath);
    }

    public void startRecording(){
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
                writeAudioDataToFile();     //Writing data into a file
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    public void writeAudioDataToFile() {
        //Write the output audio in bytes
        byte saudioBuffer[] = new byte[bufferSize];

        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath+".pcm"); //añadiendo extension
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            // gets the voice output from microphone to byte format
            recorder.read(saudioBuffer, 0, bufferSize);
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

    public void stopRecording() throws IOException {
        Log.d(TAG, "Stopping Recording");
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    public void playingRecording() throws IOException{
        // We keep temporarily filePath globally as we have only two sample sounds now..
        if (filePath==null)
            return;

        //Reading the file..
        File file = new File(filePath+".pcm"); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
        byte[] byteData = new byte[(int) file.length()];

        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            in.read( byteData );
            audioConverter.encodePcmToMp3(byteData);    //Convertir audio a MP3
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Set and push to audio track..
        int intSize = android.media.AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING);

        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS_OUT, RECORDER_AUDIO_ENCODING, intSize, AudioTrack.MODE_STREAM);
        if (at!=null) {
            at.play();
            // Write the byte array to the track
            at.write(byteData, 0, byteData.length);
            at.stop();
            at.release();
        } else {
            Log.d(TAG, "Audio track is not initialised ");
        }
    }

    public void savingRecording() throws IOException{
        File f1 = new File(filePath+".pcm"); // The location of your PCM file
        File f2 = new File(filePath+".wav"); // The location where you want your WAV file
        try {
            audioConverter.rawToWave(f1, f2);   //Convertir audio a wav
        } catch (IOException e) {
            e.printStackTrace();
        }
        audioSaver.saveToFirebase(user, index); //Guardar archivo en Firebase
    }
}
