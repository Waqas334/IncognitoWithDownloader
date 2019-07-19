package com.androidbull.incognito.browser.Rating;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.androidbull.incognito.R;


public class FirstRateActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_first_rate);
  }

  public void cancel(View view) {
    finish();
  }

  public void happy(View view) {
    startActivity(new Intent(this, HappyRatingActivity.class));
    finish();
  }

  public void confused(View view) {
    startActivity(new Intent(this, ConfusedRatingActivity.class));
    finish();
  }

  public void unhappy(View view) {
    startActivity(new Intent(this, UnhappyActivity.class));
    finish();
  }
}
