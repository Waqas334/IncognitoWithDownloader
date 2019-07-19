package com.androidbull.incognito.browser.dialgos;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.others.Preference;
import com.androidbull.incognito.browser.others.Utils.Constant;
import com.androidbull.incognito.browser.views.TOAST;
import com.facebook.FacebookSdk;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseNFaceboonConsent extends Dialog {

  private Context context;
  private Button mBtnContinueApp;

  public FirebaseNFaceboonConsent(
      @NonNull Context context) {
    super(context);
    this.context = context;
  }

  private TextView mTvConsentDesc;
  private CheckBox mFbCheckBox, mFirebaseCheckBox;
  FirebaseAnalytics firebaseAnalytics;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(Window.FEATURE_NO_TITLE); //before
    setContentView(R.layout.dialog_consent);


    firebaseAnalytics = FirebaseAnalytics.getInstance(context);

    mFbCheckBox = findViewById(R.id.cb_facebook_consnet);
    mFirebaseCheckBox = findViewById(R.id.cb_firebase_consnet);
    mBtnContinueApp = findViewById(R.id.btnContinue);

    mBtnContinueApp.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        continueApp();
      }
    });

    mTvConsentDesc = findViewById(R.id.tv_consent_desc);
    mTvConsentDesc.setMovementMethod(LinkMovementMethod.getInstance());
    mTvConsentDesc.append(Html.fromHtml(getContext().getString(R.string.learn_more)));
    FirebaseNFaceboonConsent.this.setCancelable(false);

    setCheckBoxState();

  }

  private void setCheckBoxState() {
    mFirebaseCheckBox
        .setChecked(
            Preference.isFbAnalyticalEnabled(Constant.FIREBASE_ANALYTICAL_ENABLED, context));
    mFbCheckBox
        .setChecked(Preference.isFirebaseEnabled(Constant.FB_ANALYTICAL_ENABLED, context));
  }

  private void continueApp() {
    if (!mFbCheckBox.isChecked()) {
      FacebookSdk.setLimitEventAndDataUsage(context, false);
    } else {
      FacebookSdk.setLimitEventAndDataUsage(context, true);
    }
    if (!mFirebaseCheckBox.isChecked()) {
      firebaseAnalytics.setAnalyticsCollectionEnabled(false);
    } else {
      firebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }
    Preference.setFbAnalytical(Constant.FB_ANALYTICAL_ENABLED, mFbCheckBox.isChecked(), context);
    Preference.setFirebaseAnalytical(Constant.FIREBASE_ANALYTICAL_ENABLED, mFirebaseCheckBox.isChecked(),
        context);
    FirebaseNFaceboonConsent.this.dismiss();

  }

  private String TAG = "WAQAS";

  @Override
  public void onBackPressed() {
//    super.onBackPressed();
    try {
      ApplicationInfo appinfo = context.getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      Bundle bundle = appinfo.metaData;
      String analytics_enabled = bundle.getString("firebase_analytics_collection_enabled");
      TOAST.make(context, "ANALYTICAL: " + analytics_enabled);
    } catch (NameNotFoundException e) {
      Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
    } catch (NullPointerException e) {
      Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
    }
  }
}
