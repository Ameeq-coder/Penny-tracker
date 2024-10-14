package com.example.pennytrack.Activites;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.pennytrack.BalanceDatabase;
import com.example.pennytrack.EntityClasses.BalanceEntity;
import com.example.pennytrack.Fragments.ExpenseFragment;
import com.example.pennytrack.Fragments.IncomeFragment;
import com.example.pennytrack.R;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TabLayout extends AppCompatActivity {

    private com.google.android.material.tabs.TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private BalanceDatabase balanceDatabase;

    private ImageView settingsicon;

    private Executor executor = Executors.newSingleThreadExecutor();

    @SuppressLint({"MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_layout);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        settingsicon=findViewById(R.id.settings_icon);


        balanceDatabase = BalanceDatabase.getDatabase(this);

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);


        settingsicon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TabLayout.this,settingsActivity.class);
                startActivity(intent);
            }
        });



        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Expense");
                            break;
                        case 1:
                            tab.setText("Income");
                            break;
                    }
                }).attach();

        // Check if the month has ended and update balance
        checkAndUpdateBalanceIfMonthEnded();
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ExpenseFragment();
                case 1:
                    return new IncomeFragment();
                default:
                    return new IncomeFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }

    private void checkAndUpdateBalanceIfMonthEnded() {
        Calendar currentCalendar = Calendar.getInstance();
        int currentMonth = currentCalendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based
        int currentYear = currentCalendar.get(Calendar.YEAR);

        String currentMonthString = String.format(Locale.getDefault(), "%d-%02d", currentYear, currentMonth);

        SharedPreferences sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        int lastUpdatedMonth = sharedPreferences.getInt("lastUpdatedMonth", -1);
        int lastUpdatedYear = sharedPreferences.getInt("lastUpdatedYear", -1);

        // Check if the current month is different from the last updated month
        if (lastUpdatedMonth != currentMonth || lastUpdatedYear != currentYear) {
            executor.execute(() -> updateBalanceForNewMonth(currentMonthString, sharedPreferences, currentCalendar));
        }
    }

    private void updateBalanceForNewMonth(String currentMonthString, SharedPreferences sharedPreferences, Calendar currentCalendar) {
        BalanceEntity existingBalance = balanceDatabase.balanceDao().getBalanceForMonthSync(currentMonthString);

        if (existingBalance == null) {
            // Fetch the latest balance and carry it over
            BalanceEntity latestBalance = balanceDatabase.balanceDao().getLatestBalanceSync();

            if (latestBalance != null) {
                double currentBalance = latestBalance.getAmount();

                // Insert a new row for the new month
                BalanceEntity newBalance = new BalanceEntity(currentBalance, currentBalance, currentMonthString);
                balanceDatabase.balanceDao().insert(newBalance);

                // Update SharedPreferences with the new month and year
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("lastUpdatedMonth", currentCalendar.get(Calendar.MONTH) + 1);
                editor.putInt("lastUpdatedYear", currentCalendar.get(Calendar.YEAR));
                editor.apply();
            }
        }
    }
}
