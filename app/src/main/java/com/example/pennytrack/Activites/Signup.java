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

import com.example.pennytrack.AddData;
import com.example.pennytrack.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Signup extends AppCompatActivity {



    EditText email,pass,conpass,username;
    Button signup;
    String upass,uconpass,uemail;

    TextView login;
    FirebaseAuth auth;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        email=findViewById(R.id.sttextnameid);
        pass=findViewById(R.id.stpassword);
        conpass=findViewById(R.id.stpasswordconf);
        signup=findViewById(R.id.stbtncreateid);
        login=findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Signup.this, MainActivity.class);
                startActivity(intent);
            }
        });


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uemail=email.getText().toString().trim();
                upass = pass.getText().toString().trim();
                uconpass=conpass.getText().toString().trim();
                if(uemail.isEmpty()||upass.isEmpty()||uconpass.isEmpty()){
                    Toast.makeText(Signup.this, "Enter Your Details", Toast.LENGTH_SHORT).show();
                } else if (upass.length()<8) {
                    Toast.makeText(Signup.this, "Password is Too Short ", Toast.LENGTH_SHORT).show();
                }else{
                    Signups(uemail,upass);
                }


            }
        });
    }

    private void Signups(String uemail, String upass) {
        ProgressDialog dialog = new ProgressDialog( Signup.this);
        dialog.setMessage("Creating Account");
        dialog.show();
        auth= FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(uemail, upass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Realtime();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Signup.this, "Account Already Exist", Toast.LENGTH_SHORT).show();
                dialog.dismiss();

            }
        });
    }

    private void Realtime() {
        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        DatabaseReference databaseReference=firebaseDatabase.getReference("User Profiles");
        AddData obj=new AddData(uemail,upass,uconpass);
        databaseReference.push().setValue(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent intent=new Intent(Signup.this, BalanceActivity.class);
                    startActivity(intent);
                    Toast.makeText(Signup.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Signup.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }


}