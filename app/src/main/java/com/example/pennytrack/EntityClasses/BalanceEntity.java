package com.example.pennytrack.EntityClasses;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "balance_table")
public class BalanceEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private double amount; // Current balance
    private double initialAmount; // Initial balance when created

    private String month;  // Month for which this balance is stored

    public BalanceEntity() {}


    // Constructor with all fields


    public BalanceEntity(double amount, double initialAmount, String month) {
        this.amount = amount;
        this.initialAmount = initialAmount;
        this.month = month;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getInitialAmount() {
        return initialAmount;
    }

    public void setInitialAmount(double initialAmount) {
        this.initialAmount = initialAmount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
