package com.androidbull.incognito.browser.Rating;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.DefaultFunctions;
import com.androidbull.incognito.browser.others.Preference;
import com.androidbull.incognito.browser.others.Utils.Constant;

import static com.androidbull.incognito.browser.FacebookLogger.facebookLog;

public class ConfusedRatingActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_confused_rating);
    facebookLog(this,"Confused | Rate");

  }


  public void contactUS(View view) {
    DefaultFunctions.contactUs(this);
    Preference.saveRateState(Constant.SAVE_RATE_STATE,true,this);
    finish();
  }


}
