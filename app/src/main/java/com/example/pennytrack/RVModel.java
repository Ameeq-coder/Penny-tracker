package com.example.pennytrack;

public class RVModel {
    private String title;
    private String amount;
    private int iconResId;

    private String date;



    public RVModel(String date, String title, String amount, int iconResId) {
        this.date = date;
        this.title = title;
        this.amount = amount;
        this.iconResId = iconResId;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}