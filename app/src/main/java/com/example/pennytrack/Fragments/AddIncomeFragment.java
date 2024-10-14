package com.example.pennytrack.Fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pennytrack.BalanceDatabase;
import com.example.pennytrack.DAO.BalanceDao;
import com.example.pennytrack.DAO.IncomeDao;
import com.example.pennytrack.EntityClasses.BalanceEntity;
import com.example.pennytrack.EntityClasses.IncomeEntity;
import com.example.pennytrack.R;

import java.util.Calendar;

public class AddIncomeFragment extends Fragment {
    private TextView totalBalanceTextView;
    private EditText editTextAmount;
    private TextView tvSelectDate;
    private AppCompatButton buttonAdd;
    private CardView[] categoryLayouts;
    private int selectedCategoryIndex = -1;
    private String selectedMonth;
    private BalanceDao balanceDao;
    private IncomeDao incomeDao; // Room DAO for income
    private LiveData<BalanceEntity> liveBalance;

    public AddIncomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_income, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        totalBalanceTextView = view.findViewById(R.id.addExpenseText);
        editTextAmount = view.findViewById(R.id.editTextAmount);
        tvSelectDate = view.findViewById(R.id.tv_select_date);
        buttonAdd = view.findViewById(R.id.buttonAdd);

        categoryLayouts = new CardView[]{
                view.findViewById(R.id.paysection),
                view.findViewById(R.id.giftsection),
                view.findViewById(R.id.intrestsection)
        };

        for (int i = 0; i < categoryLayouts.length; i++) {
            int index = i; // Final variable for use in lambda
            categoryLayouts[i].setOnClickListener(v -> selectCategory(index));
        }

        tvSelectDate.setOnClickListener(v -> showDatePicker());

        buttonAdd.setOnClickListener(v -> addIncome());

        balanceDao = BalanceDatabase.getDatabase(requireContext()).balanceDao(); // Initialize Room DAO
        incomeDao = BalanceDatabase.getDatabase(requireContext()).incomeDao(); // Initialize Income DAO

        // Observe the total balance
        liveBalance = balanceDao.getLatestBalance();
        liveBalance.observe(getViewLifecycleOwner(), balanceEntity -> {
            if (balanceEntity != null) {
                double amount = balanceEntity.getAmount();
                totalBalanceTextView.setText(String.format("Total Balance: $%.2f", amount));
            } else {
                totalBalanceTextView.setText("Total Balance: $0.00");
            }
        });
    }

    private void selectCategory(int index) {
        if (selectedCategoryIndex >= 0) {
            // Reset previously selected category
            categoryLayouts[selectedCategoryIndex].setBackgroundColor(Color.WHITE);
        }

        // Highlight selected category
        categoryLayouts[index].setBackgroundColor(Color.parseColor("#03A9F4")); // Orange color
        selectedCategoryIndex = index;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year1, month1, dayOfMonth) -> {
            selectedMonth = String.format("%d-%02d", year1, month1 + 1); // Format month to MM
            String fullDate = String.format("%d-%02d-%02d", year1, month1 + 1, dayOfMonth); // Format full date to YYYY-MM-DD
            tvSelectDate.setText(fullDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void addIncome() {
        String amountStr = editTextAmount.getText().toString();
        double incomeAmount;
        try {
            incomeAmount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }


        if (selectedCategoryIndex < 0) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date selection
        String fullDate = tvSelectDate.getText().toString();
        if (TextUtils.isEmpty(fullDate) || fullDate.equals("Select Date")) {
            Toast.makeText(requireContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }
        String category = getCategoryFromIndex(selectedCategoryIndex);

        new Thread(() -> {
            BalanceEntity currentBalanceEntity = liveBalance.getValue();
            double currentBalance = currentBalanceEntity != null ? currentBalanceEntity.getAmount() : 0.0;
            double initialAmount = currentBalanceEntity != null ? currentBalanceEntity.getInitialAmount() : 0.0;

            double newBalance = currentBalance + incomeAmount;
            double newInitialBalance = initialAmount + incomeAmount;

            BalanceEntity updatedBalanceEntity = currentBalanceEntity;
            updatedBalanceEntity.setAmount(newBalance);
            updatedBalanceEntity.setInitialAmount(newInitialBalance);

            balanceDao.updateBalance(updatedBalanceEntity);

            IncomeEntity income = new IncomeEntity(incomeAmount, category, fullDate);
            incomeDao.insert(income);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Income added successfully", Toast.LENGTH_SHORT).show();
                // You can navigate to another fragment or update the UI accordingly
                navigatetoincomefragment();
            });
        }).start();
    }

    private String getCategoryFromIndex(int index) {
        switch (index) {
            case 0:
                return "Pay Check";
            case 1:
                return "Gift";
            case 2:
                return "Interest";
            default:
                return "Unknown";
        }
    }


    private void navigatetoincomefragment() {
        Fragment expenseFragment = new IncomeFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, expenseFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Bundle bundle=getArguments();
            Fragment fragment=new IncomeFragment();
            fragment.setArguments(bundle);
            ((AppCompatActivity) getContext()).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container,fragment, "findThisFragment")
                    .addToBackStack(null)
                    .commit();
        }
    };

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