package com.example.pennytrack;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.pennytrack.DAO.BalanceDao;
import com.example.pennytrack.DAO.ExpenseDao;
import com.example.pennytrack.DAO.IncomeDao;
import com.example.pennytrack.EntityClasses.BalanceEntity;
import com.example.pennytrack.EntityClasses.ExpenseEntity;
import com.example.pennytrack.EntityClasses.IncomeEntity;

@Database(entities = {BalanceEntity.class, ExpenseEntity.class, IncomeEntity.class}, version = 1)
public abstract class BalanceDatabase extends RoomDatabase {
    private static BalanceDatabase instance;

    public abstract BalanceDao balanceDao();
    public abstract ExpenseDao expenseDao();

    public abstract IncomeDao incomeDao();  // Add this line


    public static synchronized BalanceDatabase getDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            BalanceDatabase.class, "balance_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}

