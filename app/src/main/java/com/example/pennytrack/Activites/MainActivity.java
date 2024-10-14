package com.example.pennytrack.Activites;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pennytrack.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    EditText uname,upass;
    Button login;
    TextView signup,forgotpass;
    String email,pass;
    FirebaseAuth auth;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uname=findViewById(R.id.email);
        upass=findViewById(R.id.password);
        login=findViewById(R.id.btncreateid);
//        forgotpass=findViewById(R.id.forgotpassw);
        signup=findViewById(R.id.signup);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email=uname.getText().toString().trim();
                pass=upass.getText().toString().trim();
                if (email.isEmpty()||pass.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter The Details", Toast.LENGTH_SHORT).show();
                }
                else {
                    Logined();
                }


            }
        });
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, Signup.class);
                startActivity(intent);
            }
        });

        
    }

    private void Logined() {
        ProgressDialog dialog = new ProgressDialog( MainActivity.this);
        dialog.setMessage("Logging in");
        dialog.show();
        auth= FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Intent intent=new Intent(MainActivity.this, BalanceActivity.class);
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Wrong Email Or Password", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
    }

}