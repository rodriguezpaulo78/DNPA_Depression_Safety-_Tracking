package com.dnpa.finalproject.depressionsafetytracking.Login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.dnpa.finalproject.depressionsafetytracking.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG="REGISTER";

    //VIEW
    EditText name,email,password;
    Button mRegisterBtn;
    CheckBox seePass;
    TextView mLoginPageBack;
    String Name,Email,Password;

    //FIREBASE
    FirebaseAuth mAuth;
    DatabaseReference mdatabase;

    //ProgressDialog
    ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        //VIEW
        name = (EditText)findViewById(R.id.editName);
        email = (EditText)findViewById(R.id.editEmail);
        password = (EditText)findViewById(R.id.editPassword);
        mRegisterBtn = (Button)findViewById(R.id.buttonRegister);
        mLoginPageBack = (TextView)findViewById(R.id.buttonLogin);
        seePass = (CheckBox) findViewById(R.id.seePassword);

        mRegisterBtn.setOnClickListener(this);
        mLoginPageBack.setOnClickListener(this);

        // for authentication using FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();
        mdatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mDialog = new ProgressDialog(this);

        onFocusChange(email);
        onFocusChange(password);

        seePass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    // show password
                    password.setTransformationMethod(new PasswordTransformationMethod());
                } else{
                    password.setTransformationMethod(null);
                }
            }
        });
    }

    public void onFocusChange(final EditText textToChange){
        textToChange.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(textToChange.getText().toString().isEmpty()){
                    if(hasFocus){
                        textToChange.setHint("");
                    }else{
                        textToChange.setHint("Enter Password");
                    }
                }
            }
        });
    }

    //CONTROLA QUE BOTON ES PRESIONADO
    @Override
    public void onClick(View v) {
        if (v== mRegisterBtn){
            UserRegister();
        }else if (v== mLoginPageBack){
            new AlertDialog.Builder(RegisterActivity.this)
                    .setTitle("Confirmación")
                    .setMessage("Esta seguro de que desea abandonar el form de registro?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

    //Metodo registro usuario FIREBASE
    private void UserRegister() {
        Name = name.getText().toString().trim();
        Email = email.getText().toString().trim();
        Password = password.getText().toString().trim();

        if (TextUtils.isEmpty(Name)){
            Toast.makeText(RegisterActivity.this, "Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }else if (TextUtils.isEmpty(Email)){
            Toast.makeText(RegisterActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }else if (TextUtils.isEmpty(Password)){
            Toast.makeText(RegisterActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }else if (Password.length()<6){
            Toast.makeText(RegisterActivity.this,"Passwor must be greater then 6 digit",Toast.LENGTH_SHORT).show();
            return;
        }
        mDialog.setMessage("Creating User please wait...");
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
        mAuth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    sendEmailVerification();
                    mDialog.dismiss();
                    OnAuth(task.getResult().getUser());
                    mAuth.signOut();
                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                }else{
                    mDialog.dismiss();
                    Toast.makeText(RegisterActivity.this,"Error al crear usuaario, revise los campos",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Email verification code using FirebaseUser object and using isSucccessful()function.
    private void sendEmailVerification() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this,"Check your Email for verification",Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
            });
        }
    }

    private void OnAuth(FirebaseUser user) {
        createAnewUser(user.getUid());
    }

    private void createAnewUser(String uid) {
        User user = BuildNewuser();
        mdatabase.child(uid).setValue(user);
    }

    private User BuildNewuser(){
        return new User(
                getDisplayName(),
                getUserEmail(),
                new Date().getTime()
        );
    }

    public String getDisplayName() {
        return Name;
    }
    public String getUserEmail() {
        return Email;
    }

}