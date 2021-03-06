package com.dnpa.finalproject.depressionsafetytracking.Login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.dnpa.finalproject.depressionsafetytracking.View.TrackingView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.dnpa.finalproject.depressionsafetytracking.R;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG="LOGIN";
    //VIEW
    EditText edtEmail, edtPassword;
    CheckBox showPass;
    Button logInButton, registerButton, forgotButton;
    String email, password;
    ProgressDialog dialog;

    //FIREBASE
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;
    FirebaseUser mUser;

    //GOOGLE
    private final static int RC_SIGN_IN = 123;

    private FirebaseAuthentication signFirebase;
    private GoogleAuthentication signGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        //VIEW
        logInButton = (Button) findViewById(R.id.buttonLogin);
        registerButton = (Button) findViewById(R.id.buttonRegister);
        forgotButton = (Button) findViewById(R.id.forgotPass);
        edtEmail = (EditText) findViewById(R.id.editEmail);
        edtPassword = (EditText) findViewById(R.id.editPassword);
        showPass = (CheckBox) findViewById(R.id.seePassword);
        dialog = new ProgressDialog(this);

        //FIREBASE
        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mUser != null) {
                    Intent intent = new Intent(LoginActivity.this, TrackingView.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    //Toast.makeText(LoginActivity.this, "Sign In Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Sign in Error", Toast.LENGTH_SHORT).show();
                    Log.d(TAG,"AuthStateChanged:Logout");
                }

            }
        };

        //Click Listener to Login
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userSign();
            }
        });

        // Adding click listener to register button.
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Opening new user registration activity using intent on button click.
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // Adding click listener to forgot button.
        forgotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!edtEmail.getText().toString().isEmpty()){
                    new AlertDialog.Builder(LoginActivity.this)
                            .setTitle("Confirmación de Cambio")
                            .setMessage("Esta seguro de que desea cambiar su contraseña?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(edtEmail.getText().toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "Email sent.");
                                                        Toast.makeText(LoginActivity.this, "Email enviado, revise su bandeja", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(LoginActivity.this, "Verify your Email", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
                }else{
                    Toast.makeText(LoginActivity.this, "Enter the Email account", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Control de EditText
        edtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(edtEmail.getText().toString().isEmpty()){
                    if(hasFocus){
                        edtEmail.setHint("");
                    }else{
                        edtEmail.setHint("Correo Electronico");
                    }
                }
            }
        });

        edtPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(edtPassword.getText().toString().isEmpty()){
                    if(hasFocus){
                        edtPassword.setHint("");
                    }else{
                        edtPassword.setHint("Enter Password");
                    }
                }
            }
        });

        showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                } else{
                    edtPassword.setTransformationMethod(null);
                }
            }
        });

        signGoogle = new GoogleAuthentication(this, this , mAuth, (Button) findViewById(R.id.google_signIn));
    }

    //METHODS LIFE CYCLE APP
    @Override
    protected void onStart() {
        super.onStart();
        //removeAuthSateListner is used  in onStart function just for checking purposes,it helps in logging you out.
        mAuth.removeAuthStateListener(mAuthListner);
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){
            Intent intent = new Intent(getApplicationContext(),TrackingView.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListner != null) {
            mAuth.removeAuthStateListener(mAuthListner);
        }
    }

    @Override
    public void onBackPressed() {
        LoginActivity.super.finish();
    }

    //Metodo par iniciar sesion via FIREBASE
    private void userSign() {
        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(LoginActivity.this, "Enter the correct Email", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(LoginActivity.this, "Enter the correct password", Toast.LENGTH_SHORT).show();
            return;
        }
        signFirebase = new FirebaseAuthentication(this, mAuth, edtEmail, edtPassword);
        signFirebase.signIn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                signGoogle.firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}