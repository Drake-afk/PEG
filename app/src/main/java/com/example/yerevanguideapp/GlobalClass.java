package com.example.yerevanguideapp;

import android.app.Application;

public class GlobalClass extends Application {

    private double userLocationLAT;
    private double UserLocationLNG;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public double getUserLocationLAT() {
        return userLocationLAT;
    }

    public void setUserLocationLAT(double userLocationLAT) {
        this.userLocationLAT = userLocationLAT;
    }

    public double getUserLocationLNG() {
        return UserLocationLNG;
    }

    public void setUserLocationLNG(double userLocationLNG) {
        UserLocationLNG = userLocationLNG;
    }
}
