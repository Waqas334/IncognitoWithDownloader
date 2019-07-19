package com.androidbull.incognito.browser.views;

import android.content.Context;
import android.widget.Toast;

import com.androidbull.incognito.BuildConfig;


public class TOAST {

  public static void make(Context context, String message) {
   if (BuildConfig.DEBUG) {
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
   }
  }
}