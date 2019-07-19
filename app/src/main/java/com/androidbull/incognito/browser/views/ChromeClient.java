package com.androidbull.incognito.browser.views;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.others.Utils;
import com.androidbull.incognito.browser.ui.MainActivity;

public class ChromeClient extends android.webkit.WebChromeClient
        implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {


    public final static int FILECHOOSER_RESULTCODE = 1;
    public static ValueCallback<Uri> mUploadMessage;
    public static ValueCallback<Uri[]> mUploadMessageLol;

    private View mVideoProgressView;
    private View mCustomView;
    private CustomViewCallback customViewCallback;


    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d("DOWNLOAD_TESTING", "MediaPlayer onPrepared was called");
        MainActivity.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        if (mVideoProgressView == null) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.mActivity);
            mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
//            mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
        }
    }


    @Override
    public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
        onShowCustomView(view, callback);
        MainActivity.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        FrameLayout customViewContainer = MainActivity.mActivity.findViewById(R.id.customViewContainer);
        // if a view already exists then immediately terminate the new one
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }
        mCustomView = view;
        customViewContainer.setVisibility(View.VISIBLE);
        customViewContainer.addView(view);
        customViewCallback = callback;


    }


    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        Log.d("DOWNLOAD_TESTING", "onShowCustomView was called was called");

        MainActivity.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        FrameLayout customViewContainer = MainActivity.mActivity.findViewById(R.id.customViewContainer);
        if (mCustomView != null) {
            callback.onCustomViewHidden();
            return;
        }


        mCustomView = view;
        customViewContainer.setVisibility(View.VISIBLE);
        customViewContainer.addView(view);
        customViewCallback = callback;

    }


    @Override
    public View getVideoLoadingProgressView() {
        if (mVideoProgressView == null) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.mActivity);
            mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
        }
        return mVideoProgressView;
    }


    @Override
    public void onHideCustomView() {
        MainActivity.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onHideCustomView();
        if (mCustomView == null)
            return;

        FrameLayout customViewContainer = MainActivity.mActivity.findViewById(R.id.customViewContainer);
        customViewContainer.setVisibility(View.GONE);
        // Hide the custom view.
        mCustomView.setVisibility(View.GONE);
        // Remove the custom view from its container.
        customViewContainer.removeView(mCustomView);
        customViewCallback.onCustomViewHidden();
        mCustomView = null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mCustomView == null)
            return;


        MainActivity.mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        FrameLayout customViewContainer = MainActivity.mActivity.findViewById(R.id.customViewContainer);

        customViewContainer.setVisibility(View.GONE);
        // Hide the custom view.
        mCustomView.setVisibility(View.GONE);
        // Remove the custom view from its container.
        customViewContainer.removeView(mCustomView);
        customViewCallback.onCustomViewHidden();
        mCustomView = null;
    }


    public void openFileChooser(ValueCallback<Uri> uploadMsg) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        MainActivity.mActivity.startActivityForResult(Intent.createChooser(i,
                MainActivity.mActivity.getResources().getString(R.string.choose_upload)), FILECHOOSER_RESULTCODE);

    }

    // For Android 3.0+
    public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");
        MainActivity.mActivity.startActivityForResult(
                Intent.createChooser(i,
                        MainActivity.mActivity.getResources().getString(R.string.choose_upload)),
                FILECHOOSER_RESULTCODE);
    }


    //For Android 4.1
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        MainActivity.mActivity.startActivityForResult(Intent.createChooser(i,
                MainActivity.mActivity.getString(R.string.choose_upload)), FILECHOOSER_RESULTCODE);

    }

    //lollipop

    @SuppressLint("NewApi")
    public boolean onShowFileChooser(WebView webView, final ValueCallback<Uri[]> filePathCallback, final FileChooserParams fileChooserParams) {

        if (mUploadMessageLol != null) {
            mUploadMessageLol.onReceiveValue(null);
            mUploadMessageLol = null;
        }

        mUploadMessageLol = filePathCallback;

        Intent intent = fileChooserParams.createIntent();
        try {
            MainActivity.mActivity.startActivityForResult(intent, FILECHOOSER_RESULTCODE);
        } catch (ActivityNotFoundException e) {
            mUploadMessageLol = null;
            Utils.msg(MainActivity.mActivity.getString(R.string.cannot_open_file_chooser), MainActivity.mActivity);

        }


        return true;
    }


    public void setmCustomView(View mCustomView) {
        this.mCustomView = mCustomView;
    }


    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        ProgressBar bnp = MainActivity.mActivity.findViewById(R.id.mainProgressBar);
        bnp.incrementProgressBy(newProgress);
        if (newProgress > 80)
            if (!view.getUrl().equals(MainActivity.HOME_PAGE_URL))
                MainActivity.otherTab(null);
            else
                MainActivity.homeTab();
    }

    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        FrameLayout mContentFrame = MainActivity.mActivity.findViewById(R.id.contentFrame);
        final CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        MainActivity.mActivity.runOnUiThread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                if (request.getOrigin().toString().equals(web.getUrl())) {
                    request.grant(request.getResources());
                } else {
                    request.deny();
                }
            }
        });
    }


}
