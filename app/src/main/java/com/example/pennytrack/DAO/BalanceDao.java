package com.example.pennytrack.DAO;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.pennytrack.EntityClasses.BalanceEntity;

@Dao
public interface BalanceDao {

    @Insert
    void insert(BalanceEntity balance);

    @Query("SELECT * FROM balance_table ORDER BY id DESC LIMIT 1")
    LiveData<BalanceEntity> getLatestBalance();  // Use LiveData here

    @Query("SELECT * FROM balance_table WHERE month = :month ORDER BY id DESC LIMIT 1")
    LiveData<BalanceEntity> getBalanceForMonth(String month);

    @Query("SELECT * FROM balance_table WHERE month = :month ORDER BY id DESC LIMIT 1")
    BalanceEntity getBalanceForMonthSync(String month);

    @Query("SELECT initialAmount FROM balance_table ORDER BY id DESC LIMIT 1")
    LiveData<Double> getInitialAmount(); // Correctly fetch the initial amount

    @Update
    void updateBalance(BalanceEntity balance);

    @Query("UPDATE balance_table SET initialAmount = :newInitialAmount WHERE id = :balanceId")
    void updateInitialAmount(int balanceId, double newInitialAmount);

    @Query("SELECT * FROM balance_table ORDER BY id DESC LIMIT 1")
    BalanceEntity getLatestBalanceSync(); // For synchronous calls


    @Query("SELECT COUNT(*) FROM balance_table WHERE month = :month")
    int checkIfBalanceExists(String month);


    @Query("SELECT initialAmount FROM balance_table WHERE month = :month LIMIT 1")
    LiveData<Double> getInitialAmountForMonth(String month);


}
