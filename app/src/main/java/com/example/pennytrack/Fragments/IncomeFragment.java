package com.example.pennytrack.Fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.pennytrack.BalanceDatabase;
import com.example.pennytrack.DAO.IncomeDao;
import com.example.pennytrack.EntityClasses.BalanceEntity;
import com.example.pennytrack.EntityClasses.ExpenseEntity;
import com.example.pennytrack.EntityClasses.IncomeEntity;
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


public class IncomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private RVAdapter adapter;
    private List<RVModel> incomeList;
    private BalanceDatabase balanceDatabase;
    private IncomeDao incomeDao;
    private TextView totalBalanceHead,tvSelectDate,percentagetext;
    private CircularProgressBar circularProgressBar;

    private Calendar calendar;

    private ImageButton button;




    public IncomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_income, container, false);
        recyclerView = view.findViewById(R.id.rv_income);
        totalBalanceHead = view.findViewById(R.id.total_balancehead_income);
        circularProgressBar = view.findViewById(R.id.circulars_income);
        tvSelectDate = view.findViewById(R.id.tv_select_date_income);
        balanceDatabase = BalanceDatabase.getDatabase(getContext());
        incomeDao = balanceDatabase.incomeDao();

        button = view.findViewById(R.id.add_income);


        button.setOnClickListener(v -> navigateToViewExpenses());


        percentagetext = view.findViewById(R.id.percentage_text_income);

        incomeList = new ArrayList<>();
        adapter = new RVAdapter(incomeList, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        calendar = Calendar.getInstance();
        setDateText(calendar);


        tvSelectDate.setOnClickListener(v -> showMonthYearPicker());


        // Load income data
        loadIncomeData();

        return view;
    }

    private void loadIncomeData() {
        // Query and observe income data
        incomeDao.getAllIncome().observe(getViewLifecycleOwner(), new Observer<List<IncomeEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChanged(List<IncomeEntity> incomeEntities) {
                updateRecyclerView(incomeEntities);
                double totalIncome = calculateTotalIncome(incomeEntities);
                observeCurrentBalanceAndInitialAmount(totalIncome);
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

    private void setDateText(Calendar calendar) {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvSelectDate.setText(displayFormat.format(calendar.getTime()));

        SimpleDateFormat queryFormat = new SimpleDateFormat("MM", Locale.getDefault());
        String monthForQuery = queryFormat.format(calendar.getTime());

        // Query and observe expenses for the selected month
        incomeDao.getIncomeForMonth(monthForQuery).observe(getViewLifecycleOwner(), new Observer<List<IncomeEntity>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChanged(List<IncomeEntity> income) {
                updateRecyclerView(income);
                double totalExpenses = calculateTotalIncome(income  );
                observeCurrentBalanceAndInitialAmount(totalExpenses);
            }
        });
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateRecyclerView(List<IncomeEntity> incomeEntities) {
        incomeList.clear();
        incomeList.addAll(convertToIncomeModel(incomeEntities));
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private List<RVModel> convertToIncomeModel(List<IncomeEntity> incomeEntities) {
        List<RVModel> models = new ArrayList<>();
        Map<String, Integer> categoryIcons = new HashMap<>();
        categoryIcons.put("Pay Check", R.drawable.paycheck);
        categoryIcons.put("Gift", R.drawable.giftbox);
        categoryIcons.put("Interest", R.drawable.intrest);

        for (IncomeEntity entity : incomeEntities) {
            int iconResId = categoryIcons.getOrDefault(entity.getCategory(), R.drawable.logo);
            models.add(new RVModel(entity.getDate(), entity.getCategory(), String.format("$%.2f", entity.getAmount()), iconResId));
        }
        return models;
    }

    private double calculateTotalIncome(List<IncomeEntity> incomeList) {
        double totalIncome = 0.0;
        for (IncomeEntity income : incomeList) {
            totalIncome += income.getAmount();
        }
        return totalIncome;
    }

    private void observeCurrentBalanceAndInitialAmount(double totalIncome) {
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
        balanceDatabase.balanceDao().getInitialAmount().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(@Nullable Double initialAmount) {
                if (initialAmount != null) {
                    updateCircularView(totalIncome, initialAmount);
                } else {
                    updateCircularView(totalIncome, 0);
                }
            }
        });
    }

    private void updateCircularView(double totalIncome, double initialAmount) {
        if (initialAmount == 0) {
            circularProgressBar.setProgress(0);
        } else {
            int percentage = (int) Math.round((totalIncome / initialAmount) * 100);
            percentage = Math.min(100, Math.max(0, percentage));
            percentagetext.setText(percentage + "%");
            circularProgressBar.setProgress(percentage);
            circularProgressBar.setProgressBarColor(Color.rgb(3,169,244));

        }
    }

    private void navigateToViewExpenses() {
        Fragment addIncomeFragment = new AddIncomeFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, addIncomeFragment);
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

    // Unregister the callback in the onDestroy() method
    @Override
    public void onDestroy() {
        super.onDestroy();
        onBackPressedCallback.remove();
    }

}
