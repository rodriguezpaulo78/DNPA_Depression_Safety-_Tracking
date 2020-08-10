package com.dnpa.finalproject.depressionsafetytracking.AudioRecording;

import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AudioToFirebase {

    private String filePath;
    //Firebase objects
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;
    StorageReference mountainsRef;
    private StorageTask mStorageTask;

    public AudioToFirebase(String filePath){
        this.filePath = filePath;
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // Create a reference to "DSTRecord.pcm"
        mountainsRef = mStorageRef.child(filePath+".pcm");
    }

    public void saveToFirebase(String user, int index){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
        Uri file2 = Uri.fromFile(new File(filePath+".wav"));
        StorageReference riversRef = mStorageRef.child(user.substring(0,index)+"/"+timeStamp+"_"+file2.getLastPathSegment());
        mStorageTask = riversRef.putFile(file2);
    }
}
