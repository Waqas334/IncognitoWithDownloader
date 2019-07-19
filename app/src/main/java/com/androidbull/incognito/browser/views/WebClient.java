package com.androidbull.incognito.browser.views;


import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.others.Utils;
import com.androidbull.incognito.browser.ui.MainActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

//import com.google.android.gms.ads.AdRequest;
//import com.google.android.gms.ads.AdView;


public class WebClient extends WebViewClient {
    private static final String TAG = "WebClient";

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Log.d(TAG, "shouldOverrideUrlLoading: ");
        String url = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            url = request.getUrl().toString();
        }
        Boolean x = getIntents(view, url);
        if (x != null) {
            return x;
        }

        return false;
    }


    @Nullable
    private Boolean getIntents(WebView view, String url) {
        if (url != null) {
            if (url.startsWith("https://play.google.com/store/")
                    || url.startsWith("market://")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                        .parse(url));
                intent.putExtra("tabNumber", Utils.Extras.getTabNumber());
                MainActivity.mActivity.startActivity(intent);
                view.reload();
                return true;
            }

        } else if (url.startsWith("https://maps.google.")
                || url.startsWith("intent://maps.google.")) {

            // Convert maps intent to normal http link
            if (url.contains("intent://")) {
                url = url.replace("intent://", "https://");
                url = url.substring(0, url.indexOf("#Intent;"));

            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse(url));
            intent.putExtra("tabNumber", Utils.Extras.getTabNumber());
            MainActivity.mActivity.startActivity(intent);
            view.reload();
            return true;
        } else if (url.startsWith("https://www.youtube.com/")
                || url.startsWith("https://m.youtube.com/")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse(url));
            intent.putExtra("tabNumber", Utils.Extras.getTabNumber());
            MainActivity.mActivity.startActivity(intent);

            return true;
        } else if (url.startsWith("intent://")) {

            Intent intent;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                intent.putExtra("tabNumber", Utils.Extras.getTabNumber());
                MainActivity.mActivity.startActivity(intent);
                view.reload();
                return true;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return false;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(MainActivity.mActivity, "Activity Not Found, Please try again later",
                        Toast.LENGTH_SHORT).show();
            }
        } else if (url.startsWith("mailto:")) {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse(url));
            intent.putExtra("tabNumber", Utils.Extras.getTabNumber());
            MainActivity.mActivity.startActivity(intent);
            view.reload();
            return true;
        } else if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                    .parse(url));
            intent.putExtra("tabNumber", Utils.Extras.getTabNumber());
            MainActivity.mActivity.startActivity(intent);
            view.reload();
            return true;
        } else if (url.startsWith("rtsp://")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            MainActivity.mActivity.startActivity(intent);
            view.reload();
            return true;
        } else if (url.startsWith("http:") || url.startsWith("https:")) {
            return false;
        } else {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                MainActivity.mActivity.startActivity(intent);
                view.reload();
                return true;
            } catch (Exception ignored) {

            }
        }

        return null;
    }

    @Deprecated
    @Override
    public boolean shouldOverrideUrlLoading(final WebView view, String url) {
        Boolean x = getIntents(view, url);
        if (x != null) {
            return x;
        }

        return false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        Log.d(TAG, "onPageStarted: called");
        MainActivity.hideFAB();
        // view.getSettings().setBlockNetworkImage(true);
        ImageView image = MainActivity.mActivity.findViewById(R.id.refreshIconMain);
        ProgressBar bnp = MainActivity.mActivity.findViewById(R.id.mainProgressBar);
        bnp.setProgress(20);
        bnp.setVisibility(View.VISIBLE);

        if (!url.equals(MainActivity.HOME_PAGE_URL)) {
            MainActivity.removeHomeBannerAd();
        } else {
            MainActivity.showHomeBannerAd();

        }

        image.setImageResource(R.drawable.cancel_refresh);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.stopLoading();
            }
        });


    }


    class ContentChecking extends AsyncTask<String, Void, String> {
        private static final String TAG = "ContentChecking";
        String URL = "";
        String contentType = "unknown";
        String AUDIO = "audio";
        String APPLICATION = "application";
        String IMAGES = "image";
        String VIDEO = "video";


        private boolean isDownloadAble() {
            if (contentType != null)
                if (contentType.contains(AUDIO) || contentType.contains(APPLICATION) || contentType.contains(IMAGES) || contentType.contains(VIDEO))
                    return true;

            return false;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            FloatingActionButton fab = MainActivity.mActivity.findViewById(R.id.download_fab);

            CustomWebView webView = MainActivity.mActivity.findViewById(R.id.mywebview);
            int index = MainActivity.webWindows.indexOf(webView);

            if (isDownloadAble()) {
                MainActivity.webWindows.get(index).setFabVisible(true);
                MainActivity.showFAB();

            } else {
                MainActivity.webWindows.get(index).setFabVisible(false);
            }

        }


        @Override
        protected String doInBackground(String... strings) {
            try {

                URL urlConnection = new URL(strings[0]);
                URLConnection c = urlConnection.openConnection();
                contentType = c.getContentType();
                URL = strings[0];


            } catch (MalformedURLException e) {


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
            }

            return contentType;
        }
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        //Checking if content is downloadable then show a FAB
        new ContentChecking().execute(url);

        final TextView titleURL = MainActivity.mActivity.findViewById(R.id.searchViewTextTitle);
        if (url.equals(MainActivity.HOME_PAGE_URL)) {
            titleURL.setText(MainActivity.mActivity.getString(R.string.search_anything));
        } else {
            titleURL.setText(Utils.getTitleForSearchBar(view.getUrl()));
        }
        Log.d("WAQAS", "OnPageFinished was called");
        ImageView image = MainActivity.mActivity.findViewById(R.id.refreshIconMain);

        ImageView forward = MainActivity.mActivity.findViewById(R.id.forwardWebPage);
        ImageView back = MainActivity.mActivity.findViewById(R.id.backWebPage);
        final FocusEditText focusEditText = MainActivity.mActivity
                .findViewById(R.id.searchBar);
        LinearLayout backLayout = MainActivity.mActivity.findViewById(R.id.backTopMenu);
        LinearLayout forwardLayout = MainActivity.mActivity
                .findViewById(R.id.forwardTopMenu);

        if (view.canGoBack() && view.getUrl() != null) {
            back.setColorFilter(Color.BLACK);
            backLayout.setClickable(true);
        } else {
            back.setColorFilter(Color.LTGRAY);
            backLayout.setClickable(false);
        }

        if (view.canGoForward() && view.getUrl() != null) {
            forward.setColorFilter(Color.BLACK);
            forwardLayout.setClickable(true);
        } else {
            forward.setColorFilter(Color.LTGRAY);
            forwardLayout.setClickable(false);
        }

        image.setImageResource(R.drawable.refresh_page_icon);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (focusEditText.isFocused()) {
                    titleURL.setText("");
                } else {
                    FrameLayout mContentFrame = MainActivity.mActivity
                            .findViewById(R.id.contentFrame);
                    final CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
                    web.reload();
                }
            }
        });

        ProgressBar bnp = MainActivity.mActivity.findViewById(R.id.mainProgressBar);

        bnp.setVisibility(View.INVISIBLE);

    }

}