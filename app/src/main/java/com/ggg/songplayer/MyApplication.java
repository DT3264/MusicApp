package com.ggg.songplayer;

import android.app.Application;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import androidx.palette.graphics.Palette;

import com.singhajit.sherlock.core.Sherlock;

public class MyApplication extends Application {

    public static ContentResolver contentResolver = null;
    public static SharedPreferences sharedPreferences = null;
    public static Bitmap mainBitmap = null;
    public static Palette.Swatch baseSwatch = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Sherlock.init(this); //Initializing Sherlock
        contentResolver = getContentResolver();
        sharedPreferences = getSharedPreferences("PlayPrefs", MODE_PRIVATE);
    }

    public static void setBitmap(Bitmap _bitmap){
        mainBitmap = _bitmap;
    }

    public static Bitmap getBitmap(){
        return mainBitmap;
    }


    public static void setSwatch(Palette.Swatch _swatch){
        baseSwatch = _swatch;
    }

    public static Palette.Swatch getBaseSwatch(){
        return baseSwatch;
    }

    public static int getBodyColor(){
        return baseSwatch.getBodyTextColor();
    }
    public static int getRGB(){
        return baseSwatch.getRgb();
    }

    public static boolean existColor(){
        return baseSwatch!=null;
    }

    public static void saveSharedPref(String key, Object val){
        if(val instanceof String) {
            sharedPreferences.edit().putString(key, (String)val).apply();
        }
        else if(val instanceof Integer){
            sharedPreferences.edit().putInt(key, (Integer) val).apply();
        }
        else if(val instanceof Float){
            sharedPreferences.edit().putFloat(key, (Float) val).apply();
        }
        else if(val instanceof Boolean){
            sharedPreferences.edit().putBoolean(key, (Boolean) val).apply();
        }
    }

    public static Object getSharedPref(String key, Object val){
        if(val instanceof String) {
            val = sharedPreferences.getString(key,"nel");
        }
        else if(val instanceof Integer){
            val = sharedPreferences.getInt(key,0);
        }
        else if(val instanceof Float){
            val = sharedPreferences.getFloat(key,0.0f);
        }
        else if(val instanceof Boolean){
            val = sharedPreferences.getBoolean(key,false);
        }
        return val;
    }
}
