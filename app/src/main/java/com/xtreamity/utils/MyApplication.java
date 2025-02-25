package com.xtreamity.utils;

import android.app.Application;
import com.google.android.gms.ads.MobileAds;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
        //real id -
        new AppOpenManager(this,"ca-app-pub-9700554883020818/3439429082");

        //test id - new AppOpenManager(this,"ca-app-pub-3940256099942544/9257395921");
    }
}
