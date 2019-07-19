package com.androidbull.incognito.browser.others;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


public class Preference {

  public static void savePreferences(String key, boolean value, Context c) {
    SharedPreferences sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(c);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(key, value);
    editor.apply();
  }


  private static SharedPreferences retainPreference(Context c) {
    return PreferenceManager.getDefaultSharedPreferences(c);
  }

  //Save and retrieve the number of times an app is opened
  public static void saveOpenState(String key, int value, Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putInt(key, value);
    editor.apply();


  }

  public static int getOpenState(String key, Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return sharedPreferences.getInt(key, 0);
  }

  //Save and retrieve the privacy URL
  public static void setFirebaseAnalytical(String key, boolean isEnabled, Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(key, isEnabled);
    editor.commit();
  }

  public static boolean isFirebaseEnabled(String key, Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return sharedPreferences.getBoolean(key, true);
  }

  //Save and retrieve the privacy URL
  public static void setFbAnalytical(String key, boolean isEnabled, Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(key, isEnabled);
    editor.commit();
  }

  public static boolean isFbAnalyticalEnabled(String key, Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return sharedPreferences.getBoolean(key, true);
  }

  //Save and retrieve the App Rate State
  public static void saveRateState(String key, boolean isRated, Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putBoolean(key, isRated);
    editor.commit();
  }

  public static boolean isRated(String key, Context context) {
    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    return sharedPreferences.getBoolean(key, false);
  }



}
