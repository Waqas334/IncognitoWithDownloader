package com.androidbull.incognito.browser.others;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebIconDatabase;
import android.webkit.WebStorage;
import android.webkit.WebViewDatabase;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.settings.SettingsPrefrence;
import com.androidbull.incognito.browser.ui.MainActivity;
import com.androidbull.incognito.browser.views.CustomWebView;
import com.androidbull.incognito.browser.views.FocusEditText;

import java.io.File;
import java.net.CookieManager;
import java.net.URL;
import java.util.Map;

import static com.androidbull.incognito.browser.others.Constants.SEARCH_ENGINE;
import static com.androidbull.incognito.browser.ui.MainActivity.HOME_PAGE_URL;


public class Utils {

  @NonNull
  public static final Map<String, String> mRequestHeaders = new ArrayMap<>();

  public static void msg(String text, Context c) {
    Toast.makeText(c, text, Toast.LENGTH_SHORT).show();
  }

  private static final String TAG = "Utils";

  public static String getTitleForSearchBar(String url) {

    try {
      URL urlObj = new URL(url);
      String host = urlObj.getHost();
      if (host != null && !host.isEmpty()) {
        return host;
      }
      if (url.startsWith("file:")) {
        String fileName = urlObj.getFile();
        if (fileName != null && !fileName.isEmpty()) {
          return fileName;
        }
      }
    } catch (Exception e) {
      // ignore
    }

    return url;
  }

  public static boolean isNetworkAvailable(Context c) {
    boolean isConnect = false;
    try {
      ConnectivityManager manager = (ConnectivityManager) c
          .getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo info = manager.getActiveNetworkInfo();
      isConnect = info != null && info.isConnected();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return isConnect;
  }


  public static void shareItemLink(final String url, MenuItem item, final Context c) {
    item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        Intents.shareUrl(url, c);
        return true;
      }
    });
  }

  public static void webViewSearch(Context c, CustomWebView WV,
                                   FocusEditText mInput) {

    String q;
    //if (Utils.isNetworkAvailable(c)) {
    q = mInput.getText().toString();
    if (q.isEmpty() || q.equals("")) {
      WV.loadUrl(HOME_PAGE_URL);
    } else if (q.contains(".") && !q.contains(" ")) {
      if (q.startsWith("http://") || q.startsWith("https://")) {
        WV.loadUrl(q);
      } else if (q.startsWith("www.")) {
        WV.loadUrl("http://" + q);
      } else if (q.startsWith("file:")) {
        WV.loadUrl(q);
      } else {
        WV.loadUrl("http://" + q);
      }
    } else if (q.startsWith("about:") || q.startsWith("file:")) {
      WV.loadUrl(q);
    } else {
      performSearch(c, WV, q);
    }

    //  } else msg("No Internet Connection",c);

  }


  private static void performSearch(Context c, CustomWebView WV, String q) {
    WV.loadUrl(createLinkFromPhrase(q, c));
  }

  public static String createLinkFromPhrase(String q, Context c) {
    return SettingsPrefrence.getSearchEngine(SEARCH_ENGINE, c) + q.replace(" ", "%20")
        .replace("+", "%2B");
  }


  public static void doNotTrack() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      mRequestHeaders.put(Constant.HEADER_DNT, "1");
    }
  }


  public static class ClearingData {

    public static void removeHistory(Context c) {
      trimCache(c);
      WebViewDatabase m = WebViewDatabase.getInstance(c);
      m.clearFormData();
      m.clearHttpAuthUsernamePassword();
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
        //noinspection deprecation
        m.clearUsernamePassword();
        //noinspection deprecation
        WebIconDatabase.getInstance().removeAllIcons();
      }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        new CookieManager().getCookieStore().removeAll();
      } else {
        //noinspection deprecation
        CookieSyncManager.createInstance(c);

      }

      for (int I = 0; I < MainActivity.webWindows.size(); I++) {
        MainActivity.webWindows.get(I).clearFormData();
      }

      WebStorage.getInstance().deleteAllData();
      c.deleteDatabase("webview.db");
      c.deleteDatabase("webViewCache.db");
    }


    static void trimCache(@NonNull Context context) {
      try {
        File dir = context.getCacheDir();

        if (dir != null && dir.isDirectory()) {
          deleteDir(dir);
        }
      } catch (Exception ignored) {

      }
    }

    static boolean deleteDir(@Nullable File dir) {
      if (dir != null && dir.isDirectory()) {
        String[] children = dir.list();
        for (String aChildren : children) {
          boolean success = deleteDir(new File(dir, aChildren));
          if (!success) {
            return false;
          }
        }
      }
      // The directory is now empty so delete it
      return dir != null && dir.delete();
    }
  }

  public static class Constant {

    //Search engines
    public static final int CONTEXT_MENU_OPEN = Menu.FIRST + 10;
    public static final int CONTEXT_MENU_OPEN_IN_NEW_TAB = Menu.FIRST + 11;
    public static final int CONTEXT_MENU_DOWNLOAD = Menu.FIRST + 13;
    public static final int CONTEXT_MENU_COPY = Menu.FIRST + 14;
    public static final int CONTEXT_MENU_SHARE = Menu.FIRST + 16;
    //Settings
    static final String HEADER_DNT = "DNT";
    public static final String RATE = "RATE";


    public static final String FB_ANALYTICAL_ENABLED = "fb_analytical_enabled";
    public static final String FIREBASE_ANALYTICAL_ENABLED = "firebase_analytical_enabled";

    public static final String SAVE_RATE_STATE = "save_rate_state";


    //Firebase Remote Config Keys

    public static final String SHOW_PROMOTED_APP = "show_promoted_app";
    public static final String PROMOTED_APP_NAME = "promoted_app_name";
    public static final String PROMOTED_APP_DESC = "promoted_app_desc";
    public static final String PROMOTED_APP_PACKAGE_NAME = "promoted_app_package";
    public static final String PROMOTED_APP_ICON = "promoted_app_icon";

    public static final String ON_RESUME_INT_AD_UNIT = "on_resume_int_ad_id";
    public static final String TABS_BANNER_AD_ID = "tabs_banner_ad";
    public static final String SMART_MENU_BANNER_AD = "smart_menu_banner_ad";
    public static final String DOWNLOAD_BANNER_AD = "download_banner_ad";
    public static final String HOME_BANNER_AD= "home_banner_ad";



  }


  public static class Extras {


    public static void xandY(View v) {
      v.setScaleX(0);
      v.setScaleY(0);
      v.animate().scaleX(1).scaleY(1).start();

    }


    public static String encodePath(String path) {
      char[] chars = path.toCharArray();
      boolean needed = false;
      for (char c : chars) {
        if (c == '[' || c == ']' || c == '|') {
          needed = true;
          break;
        }
      }
      if (!needed) {
        return path;
      }
      StringBuilder sb = new StringBuilder();
      for (char c : chars) {
        if (c == '[' || c == ']' || c == '|') {
          sb.append('%');
          sb.append(Integer.toHexString(c));
        } else {
          sb.append(c);
        }
      }
      return sb.toString();
    }


    public static int getTabNumber() {
      FrameLayout mContentFrame = MainActivity.mActivity
          .findViewById(R.id.contentFrame);

      int tabNumber = -1;
      CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
      if (web != null) {
        for (int I = 0; I < MainActivity.webWindows.size(); I++) {
          if (MainActivity.webWindows.get(I) == web) {
            tabNumber = I;
          }
        }
      }

      return tabNumber;
    }


    public static String urlWrapper(String url) {
      if (url == null) {
        return null;
      }

      String green500 = "<font color='#4CAF50'>{content}</font>";
      String gray500 = "<font color='#9E9E9E'>{content}</font>";

      if (url.startsWith("https://")) {
        String scheme = green500.replace("{content}", "https://");
        url = scheme + url.substring(8);
      } else if (url.startsWith("http://")) {
        String scheme = gray500.replace("{content}", "http://");
        url = scheme + url.substring(7);
      }

      return url;
    }
  }


  public static class Intents {


    public static boolean MyStartActivity(Intent aIntent, Context c) {
      try {
        c.startActivity(aIntent);
        return true;
      } catch (ActivityNotFoundException e) {
        return false;
      }
    }


    static void shareUrl(String url, Context c) {
      Intent share = new Intent();
      share.setAction(Intent.ACTION_SEND);
      share.setType("text/plain");
      share.putExtra(Intent.EXTRA_TEXT, url);

      try {
        MainActivity.mActivity
            .startActivity(Intent.createChooser(share, c.getString(R.string.Share)));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }


  }

}