package com.androidbull.incognito.browser.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.androidbull.incognito.R;


public class SettingsPrefrence {

    // /Save and retrieve is user already rated our app
    public static void saveUserAgent(String key, String userAgent, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, userAgent);
        editor.apply();
    }

    public static String getUserAgent(String key, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, context.getResources().getString(R.string.defaultUserAgent));
    }

    // /Save and retrieve JS Settings
    public static void saveJSState(String key, boolean isEnabled, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, isEnabled);
        editor.apply();
    }

    public static boolean getJSState(String key, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, true);
    }

    // /Save and retrieve JS Settings
    public static void saveImagesState(String key, boolean isEnabled, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, isEnabled);
        editor.apply();
    }

    public static boolean getImagesState(String key, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, true);
    }

    // /Save and retrieve JS Settings
    public static void saveFullScreenState(String key, boolean isEnabled, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, isEnabled);
        editor.apply();
    }

    public static boolean getFullScreenState(String key, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, true);
    }

    // /Save and retrieve the Search Engine
    public static void saveSearchEngine(String key, String searchEngineURL, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, searchEngineURL);
        editor.apply();
    }

    public static String getSearchEngine(String key, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, context.getResources().getString(R.string.defaultSearchEngine));
    }


}
