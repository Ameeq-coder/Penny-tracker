package com.example.pennytrack.EntityClasses;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "income_table")
public class IncomeEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount;
    private String category;
    private String date;

    public IncomeEntity(double amount, String category, String date) {
        this.amount = amount;
        this.category = category;
        this.date = date;
    }

    // Getters and Setters
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
}
