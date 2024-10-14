package com.example.pennytrack.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Dao;

import com.example.pennytrack.EntityClasses.ExpenseEntity;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    void insert(ExpenseEntity expense);

    @Query("SELECT * FROM expenses WHERE strftime('%m', date) = :month")
    LiveData<List<ExpenseEntity>> getExpensesForMonth(String month);


}
