package com.google.android.gms.samples.vision.ocrreader;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {

    Context context;
    private static final String APP_PREFERENCES = "config";
    private static final String APP_PREFERENCES_DATE = "date";
    private static final String APP_PREFERENCES_TIME = "time";
    private static final String APP_PREFERENCES_KEY = "key";
    private SharedPreferences mSettings;

    public Prefs(Context context) {
        this.context = context;
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public String  getDate(){
        return mSettings.getString(APP_PREFERENCES_DATE,"0");
    }

    public void setDate(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_DATE, id);
        editor.apply();
    }

    public String  getTime(){
        return mSettings.getString(APP_PREFERENCES_TIME,"0");
    }

    public void setTime(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_TIME, id);
        editor.apply();
    }

    public String  getKey(){
        return mSettings.getString(APP_PREFERENCES_KEY,"0");
    }

    public void setKey(String id){
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(APP_PREFERENCES_KEY, id);
        editor.apply();
    }
}
