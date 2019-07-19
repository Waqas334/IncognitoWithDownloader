package com.androidbull.incognito.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.androidbull.incognito.browser.ui.MainActivity;

import static com.androidbull.incognito.browser.FacebookLogger.facebookLog;

public class MenuActivity extends Activity {


  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent inputIntent = getIntent();
    String data = "";
    if (inputIntent.getAction() == Intent.ACTION_PROCESS_TEXT) {
      data = inputIntent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
    }
    Intent intent = new Intent(this, MainActivity.class);
    intent.putExtra("SEARCH_VALUE", data);
    intent.putExtra("IS_MENU", true);
    startActivity(intent);
    facebookLog(this, "Text Select Menu Clicked");
    finish();
  }
}
