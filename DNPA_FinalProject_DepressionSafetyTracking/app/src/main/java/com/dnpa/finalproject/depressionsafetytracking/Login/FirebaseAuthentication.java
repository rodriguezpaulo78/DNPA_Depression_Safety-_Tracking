package com.dnpa.finalproject.depressionsafetytracking.Login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dnpa.finalproject.depressionsafetytracking.View.TrackingView;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthentication {

    Context context;
    public static final String userEmail="";
    EditText Email,Password;
    FirebaseAuth mAuth;
    ProgressDialog dialog;
    FirebaseAuth.AuthStateListener mAuthListner;
    String email, password;
    //GOOGLE
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123;

    public FirebaseAuthentication(Context c, FirebaseAuth auth, EditText email, EditText password){
        context = c;
        Email=email;
        Password=password;
        mAuth = auth;
        dialog = new ProgressDialog(c);

    }

    public void signIn(){
        email = Email.getText().toString().trim();
        password = Password.getText().toString().trim();

        dialog.setMessage("Loging in please wait...");
        dialog.setIndeterminate(true);
        dialog.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    dialog.dismiss();
                    Toast.makeText(context, "Login not successfull", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    checkIfEmailVerified();
                }
            }
        });
    }

    //This function helps in verifying whether the email is verified or not.
    private void checkIfEmailVerified(){
        Intent i1 = new Intent (context, LoginActivity.class);
        FirebaseUser users=FirebaseAuth.getInstance().getCurrentUser();
        boolean emailVerified=users.isEmailVerified();
        Intent intent = new Intent(context, TrackingView.class);
        if(!emailVerified){
            Toast.makeText(context,"Verify the Email Id",Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            ((Activity)context).finish();
            context.startActivity(i1);
        }
        else {
            Email.getText().clear();
            Password.getText().clear();

            // Sending Email to Dashboard Activity using intent.
            intent.putExtra(userEmail,email);
            context.startActivity(intent);
        }
    }

}
