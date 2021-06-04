package com.giligans.queueapp.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class InitTheme extends Application {
    public static final String NIGHT_MODE = "NIGHT_MODE";
    private boolean isNightModeEnabled = false;

    private static InitTheme singleton = null;
    private static Context context;

    private InitTheme(Context context){
        this.context = context;
    }

    public static InitTheme getInstance(Context context) {

        if(singleton == null)
        {
            singleton = new InitTheme(context);
        }
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        SharedPreferences sharedPreferences = context.getSharedPreferences("Theme", Context.MODE_PRIVATE);
        this.isNightModeEnabled = sharedPreferences.getBoolean(NIGHT_MODE, false);
    }

    public boolean isNightModeEnabled() {
        return isNightModeEnabled;
    }

    public void setIsNightModeEnabled(boolean isNightModeEnabled) {
        this.isNightModeEnabled = isNightModeEnabled;

        SharedPreferences sharedPreferences = context.getSharedPreferences("Theme", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NIGHT_MODE, isNightModeEnabled);
        editor.apply();
    }
}