package com.example.pennytrack.EntityClasses;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class ExpenseEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount;
    private String category;
    private String date;



    private double monthlybalance;  // Added field for monthly balance

    public ExpenseEntity(double amount, String category, String date, double monthlybalance) {
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.monthlybalance = monthlybalance;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getMonthlybalance() {
        return monthlybalance;
    }

    public void setMonthlybalance(double monthlybalance) {
        this.monthlybalance = monthlybalance;
    }
    // Constructor with all fields

}
