package com.androidbull.incognito.browser.views;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebView;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.AddDownloadActivity;
import com.androidbull.incognito.browser.FacebookLogger;
import com.androidbull.incognito.browser.others.Constants;
import com.androidbull.incognito.browser.others.Utils;
import com.androidbull.incognito.browser.settings.SettingsPrefrence;
import com.androidbull.incognito.browser.ui.MainActivity;

import static com.androidbull.incognito.browser.others.Constants.IMAGES_STATE;
import static com.androidbull.incognito.browser.others.Constants.JS_STATE;

public class CustomWebView extends WebView {


    private static final String[] PERMISSIONS = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    boolean fabVisible = false;

    public boolean isFabVisible() {
        return fabVisible;
    }

    public void setFabVisible(boolean fabVisible) {
        this.fabVisible = fabVisible;
    }


    public CustomWebView(Context context) {
        super(context);

    }


    @SuppressLint("SetJavaScriptEnabled")
    public CustomWebView(Context context, AttributeSet attrs, String url) {
        super(context, attrs);
        this.loadUrl(url, Utils.mRequestHeaders);
        this.setId(R.id.mywebview);
        downloadListner();
        refreshSettings();


    }


    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private static final String TAG = "CustomWebView";

    private void refreshSettings() {
        Utils.doNotTrack();

        Log.d(TAG, "URL: " + this.getUrl());
        this.getSettings().setLoadsImagesAutomatically(SettingsPrefrence.getImagesState(IMAGES_STATE, getContext()));
        this.getSettings().setJavaScriptEnabled(SettingsPrefrence.getJSState(JS_STATE, getContext()));
        this.getSettings().setLoadWithOverviewMode(true);
        this.getSettings().setUseWideViewPort(true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(this,
                    false);

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //noinspection deprecation
            this.getSettings().setSavePassword(false);
        }


        this.getSettings().setSupportZoom(true);

        this.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        this.setScrollbarFadingEnabled(false);


        this.getSettings().setSaveFormData(false);

        this.getSettings().setBuiltInZoomControls(true);
        this.getSettings().setDisplayZoomControls(false);


        this.getSettings().setDatabaseEnabled(false);
        this.getSettings().setAppCacheEnabled(false);
        this.getSettings().setGeolocationEnabled(false);


        this.getSettings().setTextZoom(100);


        this.setBackgroundColor(Color.WHITE);
        this.setScrollbarFadingEnabled(false);
        this.setNetworkAvailable(true);
        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.getSettings().setUserAgentString(SettingsPrefrence.getUserAgent(Constants.USER_AGENT, getContext()));


    }


    private void downloadListner() {

        this.setWebViewClient(new WebClient());
        this.setWebChromeClient(new ChromeClient());


        this.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                FacebookLogger.facebookLog(MainActivity.mActivity, "Download Listener called");
                Intent i = new Intent(MainActivity.mActivity, AddDownloadActivity.class);
                i.putExtra(Intent.EXTRA_TEXT, url);
                MainActivity.mActivity.startActivity(i);


            }
        });
    }
}
