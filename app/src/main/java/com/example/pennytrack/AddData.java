package com.example.pennytrack;

public class AddData {
    String  email, pass, conpass;


    public AddData(String email, String pass, String conpass) {
        this.email = email;
        this.pass = pass;
        this.conpass = conpass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getConpass() {
        return conpass;
    }

    public void setConpass(String conpass) {
        this.conpass = conpass;
    }
}