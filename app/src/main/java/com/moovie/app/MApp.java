package com.moovie.app;

import android.app.Application;

/**
 Manages the AppStore instance.
 */
public class MApp extends Application {
    /**
     Create the application with an AppStore instance.
     */
    @Override public void onCreate() {
        super.onCreate();
        // Initialize AppStore and preload logos
        AppStore.init(this);
    }
}