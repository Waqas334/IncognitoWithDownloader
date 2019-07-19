package com.androidbull.incognito.browser.Rating;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.DefaultFunctions;
import com.androidbull.incognito.browser.others.Preference;
import com.androidbull.incognito.browser.others.Utils.Constant;

import java.util.List;

import static com.androidbull.incognito.browser.FacebookLogger.facebookLog;

public class HappyRatingActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_happy_rating);
    facebookLog(this,"Happy | Rate");
  }

  public void writeReview(View view) {
    DefaultFunctions.gotoGooglePlayPage(this);
    Preference.saveRateState(Constant.SAVE_RATE_STATE,true,this);
    finish();
  }

  public void contactUS(View view) {
    DefaultFunctions.contactUs(this);
    Preference.saveRateState(Constant.SAVE_RATE_STATE,true,this);
    finish();
  }

  public void tweetAboutRabbit(View view) {
    initShareIntent("twi");
    finish();
  }


  private void initShareIntent(String type) {
    boolean found = false;
    Intent share = new Intent(Intent.ACTION_SEND);
    share.setType("text/plain");
    // gets the list of intents that can be loaded.
    List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(share, 0);
    if (!resInfo.isEmpty()) {
      for (ResolveInfo info : resInfo) {
        if (info.activityInfo.packageName.toLowerCase().contains(type) ||
            info.activityInfo.name.toLowerCase().contains(type)) {
          share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " " + getString(R.string.is_amazing));
          share.putExtra(Intent.EXTRA_TEXT,
              getString(R.string.share_this_app_with)
                  + "https://play.google.com/store/apps/details?id=" + getPackageName());
//          share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(myPath)) ); // Optional, just if you wanna share an image.
          share.setPackage(info.activityInfo.packageName);
          found = true;
          break;
        }
      }
      if (!found) {
        return;
      }

      startActivity(Intent.createChooser(share, "Select"));
    }
  }


  public void tellYourFriendFb(View view) {
    initShareIntent("face");
    finish();
  }


}
