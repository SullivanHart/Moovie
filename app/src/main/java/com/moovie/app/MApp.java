package com.moovie.app;

import android.app.Application;

public class MApp extends Application {
    @Override public void onCreate() {
        super.onCreate();
        // Initialize AppStore and preload logos
        AppStore.init(this);
    }
}