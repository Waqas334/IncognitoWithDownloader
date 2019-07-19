package com.androidbull.incognito.browser;

import android.content.Context;
import android.util.Log;

import com.androidbull.incognito.BuildConfig;
import com.facebook.appevents.AppEventsLogger;

public class FacebookLogger {

  public static void facebookLog(Context context, String eventName) {
    AppEventsLogger.newLogger(context).logEvent(eventName);
    if (BuildConfig.DEBUG) {
      Log.d("FACEBOOK_LOGS", eventName);
    }

  }

}
