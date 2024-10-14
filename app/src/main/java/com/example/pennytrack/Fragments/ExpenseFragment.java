package com.example.pennytrack.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.pennytrack.BalanceDatabase;
import com.example.pennytrack.DAO.ExpenseDao;
import com.example.pennytrack.EntityClasses.BalanceEntity;
import com.example.pennytrack.EntityClasses.ExpenseEntity;
import com.example.pennytrack.R;
import com.example.pennytrack.RVAdapter;
import com.example.pennytrack.RVModel;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseFragment extends Fragment {
    private RecyclerView recyclerView;
    private RVAdapter adapter;
    private List<RVModel> list;
    private TextView totalBalanceHead, tvSelectDate;
    private BalanceDatabase balanceDatabase;
    private ExpenseDao expenseDao;
    private ImageButton button;
    private LiveData<List<ExpenseEntity>> liveExpenses; // LiveData for expenses
    private Calendar calendar;
    CircularProgressBar circularProgressBar;


    TextView percentagetext;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        recyclerView = view.findViewById(R.id.rv);
        totalBalanceHead = view.findViewById(R.id.total_balancehead);
        tvSelectDate = view.findViewById(R.id.tv_select_date);
        button = view.findViewById(R.id.add_expense);
        circularProgressBar = view.findViewById(R.id.circulars);
        circularProgressBar.setProgress(0); // Start at 0

        balanceDatabase = BalanceDatabase.getDatabase(getContext());
        expenseDao = balanceDatabase.expenseDao();

        list = new ArrayList<>();
        adapter = new RVAdapter(list, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        // Initialize the calendar and set the current month in tvSelectDate
        calendar = Calendar.getInstance();
        setDateText(calendar);
percentagetext =view.findViewById(R.id.percentage_text);
        // Set an onClickListener on tvSelectDate to open the DatePickerDialog
        tvSelectDate.setOnClickListener(v -> showMonthYearPicker());

        button.setOnClickListener(v -> navigateToViewExpenses());



        return view;
    }

    private void setDateText(Calendar calendar) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvSelectDate.setText(displayFormat.format(calendar.getTime()));

        SimpleDateFormat queryFormat = new SimpleDateFormat("MM", Locale.getDefault());
        String monthForQuery = queryFormat.format(calendar.getTime());


        SimpleDateFormat queryFormatforinitialbalance = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        String monthForQueryforinitialbalance = queryFormatforinitialbalance.format(calendar.getTime());

        // Query and observe expenses for the selected month
        expenseDao.getExpensesForMonth(monthForQuery).observe(getViewLifecycleOwner(), new Observer<List<ExpenseEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChanged(List<ExpenseEntity> expenses) {
                updateRecyclerView(expenses);
                double totalExpenses = calculateTotalExpenses(expenses);
                observeCurrentBalanceAndInitialAmount(totalExpenses,monthForQueryforinitialbalance);
            }
        });
    }

    private void showMonthYearPicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            setDateText(calendar);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        try {
            // Hide the day picker if it exists
            datePickerDialog.getDatePicker().findViewById(
                    getResources().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
        } catch (NullPointerException e) {
            e.printStackTrace(); // Log the error or handle it as needed
        }

        datePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateRecyclerView(List<ExpenseEntity> expenses) {
        list.clear();
        list.addAll(convertToExpensesModel(expenses));
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<RVModel> convertToExpensesModel(List<ExpenseEntity> expenseEntities) {
        List<RVModel> models = new ArrayList<>();
        Map<String, Integer> categoryIcons = new HashMap<>();
        categoryIcons.put("Health", R.drawable.health);
        categoryIcons.put("Leisure", R.drawable.wallet);
        categoryIcons.put("Home", R.drawable.home_icon);
        categoryIcons.put("Education", R.drawable.education_icon);
        categoryIcons.put("Travel", R.drawable.travel);
        categoryIcons.put("Gifts", R.drawable.giftbox);
        categoryIcons.put("Groceries", R.drawable.trolley);
        categoryIcons.put("Cafeteria", R.drawable.dine);

        for (ExpenseEntity entity : expenseEntities) {
            int iconResId = categoryIcons.getOrDefault(entity.getCategory(), R.drawable.logo);
            models.add(new RVModel(entity.getDate(), entity.getCategory(), String.format("$%.2f", entity.getAmount()), iconResId));
        }
        return models;
    }

    private double calculateTotalExpenses(List<ExpenseEntity> expensesList) {
        double totalExpenses = 0.0;
        for (ExpenseEntity expense : expensesList) {
            totalExpenses += expense.getAmount();
        }
        return totalExpenses;
    }

    private void observeCurrentBalanceAndInitialAmount(double totalExpenses,String monthForQueryforinitialbalance) {
        // Fetch current balance for totalBalanceHead
        balanceDatabase.balanceDao().getLatestBalance().observe(getViewLifecycleOwner(), new Observer<BalanceEntity>() {
            @Override
            public void onChanged(@Nullable BalanceEntity currentBalanceEntity) {
                if (currentBalanceEntity != null) {
                    double currentAmount = currentBalanceEntity.getAmount();
                    totalBalanceHead.setText(String.format("$%.2f", currentAmount));
                } else {
                    totalBalanceHead.setText("$0.00");
                }
            }
        });

        // Fetch initial amount for circular progress view
        balanceDatabase.balanceDao().getBalanceForMonth(monthForQueryforinitialbalance).observe(getViewLifecycleOwner(), new Observer<BalanceEntity>() {
            @Override
            public void onChanged(@Nullable BalanceEntity balanceEntity) {
                if (balanceEntity != null) {
                    double initialAmount = balanceEntity.getInitialAmount();
                    Log.d("BalanceQuery", "Fetched Initial Amount: " + initialAmount);
                    Log.d("BalanceQuery", "Month for Query: " + monthForQueryforinitialbalance);
                    updateCircularView(totalExpenses, initialAmount);
                } else {
                    Log.d("BalanceQuery", "No balance found for month: " + monthForQueryforinitialbalance);
                    updateCircularView(totalExpenses, 0); // Handle case where no balance found
                }
            }
        });

    }

    private void updateCircularView(double totalExpenses, double initialAmount) {


        Log.d("ExpenseFragment", "Total Expenses: " + totalExpenses);
        Log.d("ExpenseFragment", "Initial Amount: " + initialAmount);



        if (initialAmount == 0) {
            Log.d("ExpenseFragment", "Initial Amount is 0, setting progress to 0.");
            circularProgressBar.setProgress(0);
            percentagetext.setText("0%");

        } else {
            // Calculate the percentage based on the initial total amount (initialAmount)
            int percentage = (int) Math.round((totalExpenses / initialAmount) * 100);
            Log.d("ExpenseFragment", "Calculated Percentage: " + percentage);
            // Ensure the percentage is clamped between 0 and 100
            percentage = Math.min(100, Math.max(0, percentage));
            Log.d("ExpenseFragment", "Clamped Percentage: " + percentage);
            percentagetext.setText(String.format("%d%%", percentage));


            if (percentage >= 100) {
                Log.d("ExpenseFragment", "Expenses exceed or match the initial amount, setting color to RED.");
                circularProgressBar.setProgressBarColor(Color.RED);
            } else {
                Log.d("ExpenseFragment", "Within budget, setting color to GREEN.");
                circularProgressBar.setProgressBarColor(Color.rgb(255,193,7));
            }

            Log.d("ExpenseFragment", "Updating CircularProgressBar with percentage: " + percentage);
            circularProgressBar.setProgressWithAnimation(percentage); // animate the progress
        }
    }




    private void navigateToViewExpenses() {
        Fragment addExpFragment = new AddExpFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, addExpFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Exit();
        }
    };

    private void Exit() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Exit Penny Tracker");
        builder.setMessage("Are you sure you want to exit the Penny Tracker?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finishAffinity();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }




    // Register the callback in the onCreate() method
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onBackPressedCallback.remove();
    }



}
