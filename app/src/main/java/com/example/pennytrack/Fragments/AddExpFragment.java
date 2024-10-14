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
import com.example.pennytrack.DAO.ExpenseDao;
import com.example.pennytrack.EntityClasses.BalanceEntity;
import com.example.pennytrack.EntityClasses.ExpenseEntity;
import com.example.pennytrack.R;

import java.util.Calendar;

public class AddExpFragment extends Fragment {
    private TextView totalBalanceTextView;
    private EditText editTextAmount;
    private TextView tvSelectDate;
    private AppCompatButton buttonAdd;
    private CardView[] categoryLayouts;
    private int selectedCategoryIndex = -1;
    private String selectedMonth;
    private BalanceDao balanceDao; // Room DAO
    private ExpenseDao expenseDao; // Room DAO for expenses
    private LiveData<BalanceEntity> liveBalance;



    private double initialBalance;




    private double percentage;


    public AddExpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_exp, container, false);
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
                view.findViewById(R.id.healthSection),
                view.findViewById(R.id.leisureSection),
                view.findViewById(R.id.homeSection),
                view.findViewById(R.id.educationSection),
                view.findViewById(R.id.giftsSection),
                view.findViewById(R.id.groceriesSection),
                view.findViewById(R.id.travelSection),
                view.findViewById(R.id.cafeteriaSection)
        };

        for (int i = 0; i < categoryLayouts.length; i++) {
            int index = i; // Final variable for use in lambda
            categoryLayouts[i].setOnClickListener(v -> selectCategory(index));
        }

        tvSelectDate.setOnClickListener(v -> showDatePicker());

        buttonAdd.setOnClickListener(v -> addExpense());

        balanceDao = BalanceDatabase.getDatabase(requireContext()).balanceDao(); // Initialize Room DAO
        expenseDao = BalanceDatabase.getDatabase(requireContext()).expenseDao(); // Initialize Expense DAO

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

        liveBalance.observe(getViewLifecycleOwner(), balanceEntity -> {
            if (balanceEntity != null) {
                initialBalance = balanceEntity.getInitialAmount();
                // You can also update your UI with the new initial balance if needed
                // e.g., balanceTextView.setText(String.format("Initial Balance: $%.2f", initialBalance));

//                Toast.makeText(requireContext(), "Initial Balance: $"+initialBalance, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void selectCategory(int index) {
        if (selectedCategoryIndex >= 0) {
            // Reset previously selected category
            categoryLayouts[selectedCategoryIndex].setCardBackgroundColor(Color.WHITE);
        }

        // Highlight selected category
        categoryLayouts[index].setCardBackgroundColor(Color.parseColor("#FFA500")); // Orange color
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

    private void addExpense() {





        // Validate category selection
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

        // Validate amount
        String amountStr = editTextAmount.getText().toString();
        double expenseAmount;
        try {
            expenseAmount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            BalanceEntity currentBalanceEntity = liveBalance.getValue();
            if (currentBalanceEntity != null) {

                double initialBalance = this.initialBalance;

                if (expenseAmount > currentBalanceEntity.getAmount()) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(requireContext(), "Not enough balance", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                double newBalance = currentBalanceEntity.getAmount() - expenseAmount;

                // Update the current balance (remaining balance after expense)
                BalanceEntity updatedBalanceEntity = currentBalanceEntity;
                updatedBalanceEntity.setAmount(newBalance);
                balanceDao.updateBalance(updatedBalanceEntity);

                // Create an ExpenseEntity with correct balance references
                String category = getCategoryFromIndex(selectedCategoryIndex);
                ExpenseEntity expense = new ExpenseEntity(expenseAmount, category, fullDate, initialBalance); // Use initialBalance
                expenseDao.insert(expense);

                // Notify the user and update the UI
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show();
                    navigateToExpenseFragmentWithPercentage();
                });
            }
        }).start();
    }

    private String getCategoryFromIndex(int index) {
        switch (index) {
            case 0:
                return "Health";
            case 1:
                return "Leisure";
            case 2:
                return "Home";
            case 3:
                return "Education";
            case 4:
                return "Gifts";
            case 5:
                return "Groceries";
            case 6:
                return "Travel";
            case 7:
                return "Cafeteria";
            default:
                return "Unknown";
        }
    }

    private void navigateToExpenseFragmentWithPercentage() {
        Fragment expenseFragment = new ExpenseFragment();
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, expenseFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Bundle bundle=getArguments();
            Fragment fragment=new ExpenseFragment();
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

