package com.dnpa.finalproject.depressionsafetytracking.Login;

import android.app.AlertDialog;
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
import androidx.appcompat.app.AppCompatActivity;
import com.dnpa.finalproject.depressionsafetytracking.R;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private UserToFirebase userSaver;

    //VIEW
    EditText edtName, edtEmail, edtPassword;
    Button mRegisterBtn;
    CheckBox seePass;
    TextView mLoginPageBack;
    String Name,Email,Password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        //VIEW
        edtName = (EditText)findViewById(R.id.editName);
        edtEmail = (EditText)findViewById(R.id.editEmail);
        edtPassword = (EditText)findViewById(R.id.editPassword);
        mRegisterBtn = (Button)findViewById(R.id.buttonRegister);
        mLoginPageBack = (TextView)findViewById(R.id.buttonLogin);
        seePass = (CheckBox) findViewById(R.id.seePassword);
        mRegisterBtn.setOnClickListener(this);
        mLoginPageBack.setOnClickListener(this);

        //Control de EditText
        edtName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(edtName.getText().toString().isEmpty()){
                    if(hasFocus){
                        edtName.setHint("");
                    }else{
                        edtName.setHint("Enter username");
                    }
                }
            }
        });

        edtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(edtEmail.getText().toString().isEmpty()){
                    if(hasFocus){
                        edtEmail.setHint("");
                    }else{
                        edtEmail.setHint("Enter e-mail");
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
                        edtPassword.setHint("Enter password");
                    }
                }
            }
        });

        //Muestra el password escrito
        seePass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked){
                    edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                } else{
                    edtPassword.setTransformationMethod(null);
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
        Name = edtName.getText().toString().trim();
        Email = edtEmail.getText().toString().trim();
        Password = edtPassword.getText().toString().trim();

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
            Toast.makeText(RegisterActivity.this,"Passwor must be greater than 6 digit",Toast.LENGTH_SHORT).show();
            return;
        }

        userSaver = new UserToFirebase(this, Name, Email, Password);
        userSaver.saveUserFirebase();
    }
}