package com.example.pennytrack.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.pennytrack.EntityClasses.ExpenseEntity;
import com.example.pennytrack.EntityClasses.IncomeEntity;

import java.util.List;

@Dao
public interface IncomeDao {
    @Insert
    void insert(IncomeEntity income);

    @Query("SELECT * FROM income_table ORDER BY date DESC")
    LiveData<List<IncomeEntity>> getAllIncome();

    @Query("SELECT * FROM income_table WHERE strftime('%m', date) = :month")
    LiveData<List<IncomeEntity>> getIncomeForMonth(String month);


}