package com.example.pennytrack.Activites;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pennytrack.BalanceDatabase;
import com.example.pennytrack.EntityClasses.BalanceEntity;
import com.example.pennytrack.EntityClasses.ExpenseEntity;
import com.example.pennytrack.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BalanceActivity extends AppCompatActivity {

    private EditText etBalance;
    private Button btnNext;
    private BalanceDatabase balanceDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        etBalance = findViewById(R.id.et_balance);
        btnNext = findViewById(R.id.btn_next);

        balanceDatabase = BalanceDatabase.getDatabase(this);

//        new Thread(() -> {
//            BalanceEntity existingBalance = balanceDatabase.balanceDao().getLatestBalanceSync();
//            if (existingBalance != null) {
//                // Balance exists, redirect to TabLayout
//                runOnUiThread(() -> {
//                    Intent intent = new Intent(BalanceActivity.this, TabLayout.class);
//                    startActivity(intent);
//                    finish();
//                });
//            } else {
//                // Set up button click listener
//                runOnUiThread(() -> btnNext.setOnClickListener(v -> saveBalanceAndProceed()));
//            }
//        }).start();



        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveBalanceAndProceed();
            }
        });
    }

    // BalanceActivity.java



    private void saveBalanceAndProceed() {
        double balanceAmount = Double.parseDouble(etBalance.getText().toString());
        String currentMonth = getCurrentMonth();

        // Insert balance into the database
        new Thread(() -> {
            BalanceEntity balance = new BalanceEntity(balanceAmount, balanceAmount, currentMonth);
            balanceDatabase.balanceDao().insert(balance);

            runOnUiThread(() -> {
                Intent intent = new Intent(BalanceActivity.this, TabLayout.class);
                startActivity(intent);
                finish();
            });
        }).start();
    }

    private String getCurrentMonth() {
        // Use the current month
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
    }



}
