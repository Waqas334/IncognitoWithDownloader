package com.androidbull.incognito.browser.ui;


import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.androidbull.incognito.BuildConfig;
import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.AddDownloadActivity;
import com.androidbull.incognito.browser.DownloadMainActivity;
import com.androidbull.incognito.browser.FacebookLogger;
import com.androidbull.incognito.browser.FixedBottomSheetDialog;
import com.androidbull.incognito.browser.Rating.FirstRateActivity;
import com.androidbull.incognito.browser.SettingsActivity;
import com.androidbull.incognito.browser.dialgos.CoffeeDialog;
import com.androidbull.incognito.browser.dialgos.FirebaseNFaceboonConsent;
import com.androidbull.incognito.browser.others.Constants;
import com.androidbull.incognito.browser.others.Preference;
import com.androidbull.incognito.browser.others.Utils;
import com.androidbull.incognito.browser.others.Utils.Constant;
import com.androidbull.incognito.browser.settings.SettingsPrefrence;
import com.androidbull.incognito.browser.views.ChromeClient;
import com.androidbull.incognito.browser.views.CustomWebView;
import com.androidbull.incognito.browser.views.FocusEditText;
import com.androidbull.incognito.browser.views.SearchBarAdapter;
import com.androidbull.incognito.browser.views.SwipeDismissListViewTouchListener;
import com.androidbull.incognito.browser.views.TabsAdapter;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.androidbull.incognito.browser.FacebookLogger.facebookLog;
import static com.androidbull.incognito.browser.others.Constants.FULL_SCREEN_STATE;


public class MainActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    private static final String TAG = "MainActivity";
    public static String HOME_PAGE_URL = "file:///android_asset/english.html";


    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static Activity mActivity;
    public static Vector<CustomWebView> webWindows;
    private static InputMethodManager imm;

    private String[] PERMISSIONS2;
    private ClipboardManager mClipboardManager;
    private ClipData mClipData;
    private SearchBarAdapter suggestionsAdapter;
    private List<String> responses;
    private Handler mHandler;
    private Runnable m_Runnable;
    private ProgressBar progress;

    private AdView tabsAdView;
    private RelativeLayout adContainerLinearLayout;
    private TabsAdapter mTabsAdapter;


    @Bind(R.id.mainWebLayout)
    RelativeLayout mMainWebLayout;
    @Bind(R.id.searchViewTextTitle)
    TextView mSerachTitleTextView;
    @Bind(R.id.serachViewFocus)
    LinearLayout mSearchViewFocus;
    @Bind(R.id.searchViewNormal)
    LinearLayout mSearchViewNormal;
    @Bind(R.id.searchBar)
    FocusEditText mInput;
    @Bind(R.id.clearAll)
    ImageView mclearAll;
    @Bind(R.id.coordinatorLayout)
    CoordinatorLayout mMainLayout;
    @Bind(R.id.contentFrame)
    FrameLayout mContentFrame;
    @Bind(R.id.notif_count_text)
    TextView mTabButtonText;
    @Bind(R.id.notif_count)
    ImageView mTabButton;
    @Bind(R.id.forwardWebPage)
    ImageView forward;
    @Bind(R.id.backWebPage)
    ImageView back;
    @Bind(R.id.newTabButtonMainAct)
    LinearLayout mNewTabIconMAin;
    @Bind(R.id.mainMenuIcon333)
    LinearLayout mSmartMenuIcon;
    @Bind(R.id.rightDrawerLayout2)
    RelativeLayout mTabLayout;
    @Bind(R.id.tabs_list_view)
    ListView mTabsListView;
    @Bind(R.id.tabs_bg)
    LinearLayout tabsBg;


    @OnClick(R.id.notificationTabIcon)
    public void openNewTab() {
        mInput.clearFocus();
        showTabsView();
        hideFAB();
        mTabsAdapter = new TabsAdapter(MainActivity.this);
        mTabsListView.setAdapter(mTabsAdapter);
        mTabsAdapter.notifyDataSetChanged();
        mTabButtonText.setText(String.valueOf(setNotifCount()));

        if (mTabLayout.getVisibility() == View.VISIBLE && !isPaid && isDataFetched) {
            //Creating a banner ad on runtime here
            adContainerLinearLayout.setVisibility(View.VISIBLE);
            String bannerAdId = (String) firebaseDefaultHashMap.get(Constant.TABS_BANNER_AD_ID);
            tabsAdView = new AdView(this, bannerAdId, AdSize.BANNER_HEIGHT_50);
            tabsAdView.loadAd();
            adContainerLinearLayout.addView(tabsAdView);
            Log.d("TESTING_Ads", "mTabLayout is visible");

        }
        facebookLog(this, "Tab Icon Clicked");
    }

    @OnClick(R.id.backTopMenu)
    public void mBackButton() {
        /* Back Button in the Bottom Bar*/
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        if (web.canGoBack() && web.getUrl() != null) {
            web.goBack();
        } else if (!web.canGoBack() || web.getUrl() == null || web.getUrl().equals("about:blank")) {
            back.setColorFilter(Color.LTGRAY);
            Log.d("WAQAS", "WebCanNotGobacl");
        }

    }

    @OnClick(R.id.forwardTopMenu)
    public void mForwardButton() {
        /* Forward Button in the Bottom Bar*/
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        if (web.canGoForward()) {
            web.goForward();
        } else {
            forward.setColorFilter(Color.LTGRAY);
        }
    }


    @OnClick(R.id.home_ll)
    public void mHome() {
        /*Home Button is pressed*/
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        web.loadUrl(HOME_PAGE_URL);
        facebookLog(this, "Home button clicked");


    }

    @OnClick(R.id.searchViewTextTitle)
    public void SearchNormal() {
        mSearchViewNormal.setVisibility(View.INVISIBLE);
        mSearchViewFocus.setVisibility(View.VISIBLE);
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);

        try {
            if (web.getUrl() != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    mInput.setText(
                            Html.fromHtml(Utils.Extras.urlWrapper(web.getUrl()), Html.FROM_HTML_MODE_LEGACY),
                            EditText.BufferType.SPANNABLE);
                } else {
                    mInput.setText(Html.fromHtml(Utils.Extras.urlWrapper(web.getUrl())),
                            EditText.BufferType.SPANNABLE);
                }
            }
        } catch (NullPointerException e) {
            Log.d("WAQAS", "Null Pointer exception");
        }
        mInput.selectAll();
        mInput.setSelectAllOnFocus(true);

        Utils.Extras.xandY(mclearAll);

        if (!mInput.getText().toString().equals("")) {
            Log.e("WAQAS", "mInput is empty");
            mInput.requestFocus();
            mInput.selectAll();
            mInput.setSelectAllOnFocus(true);
        }
    }


    public static boolean fabVisible = false;

    public static void hideFAB() {
        FloatingActionButton downloadFAB = MainActivity.mActivity.findViewById(R.id.download_fab);
        if (downloadFAB.getVisibility() == View.VISIBLE) {
            fabVisible = true;
            Animation animationUtils = AnimationUtils.loadAnimation(MainActivity.mActivity, R.anim.zoom_out);
            downloadFAB.startAnimation(animationUtils);
            animationUtils.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    downloadFAB.setVisibility(View.GONE);

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    public static void showFAB() {
        facebookLog(MainActivity.mActivity, "Download FAB Displayed");

        fabVisible = false;
        FloatingActionButton fab = MainActivity.mActivity.findViewById(R.id.download_fab);
        fab.setVisibility(View.VISIBLE);
        Animation animationUtils = AnimationUtils.loadAnimation(MainActivity.mActivity, R.anim.zoom_in);
        fab.startAnimation(animationUtils);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.mActivity, "Download FAB Clicked");
                CustomWebView web = MainActivity.mActivity.findViewById(R.id.mywebview);
                String url = web.getUrl();
                Intent i = new Intent(MainActivity.mActivity, AddDownloadActivity.class);
                i.putExtra(Intent.EXTRA_TEXT, url);
                MainActivity.mActivity.startActivity(i);

            }
        });

    }


    private void showTabsView() {

        mTabLayout.setVisibility(View.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        mTabLayout.startAnimation(slideUp);


        tabsBg.setVisibility(View.VISIBLE);
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(300);
        tabsBg.startAnimation(fadeIn);
    }

    private void hideTabsView() {
        /*This method is responsible for hiding Tabs Layout and FAB*/
//        if (fabVisible) {
//            showFAB();
//            fabVisible = false;
//        }
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(300);
        tabsBg.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tabsBg.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        findViewById(R.id.tabs_bg).setVisibility(View.VISIBLE);
        mTabLayout.startAnimation(slideDown);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTabLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void onLonkClickMenuItems() {

        mSerachTitleTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
                if (web.getUrl() != null) {
                    copyLink("input", web.getUrl());
                } else {
                    if (!Utils.isNetworkAvailable(MainActivity.this)) {
                        Utils.msg(getString(R.string.network_unavailable), MainActivity.this);
                    }
                }
                return true;
            }
        });
    }

    private void openURLInNewTab(String url) {
        webWindows.add(new CustomWebView(this, null, url));

        mContentFrame.removeAllViews();
        mContentFrame.addView(webWindows.get(webWindows.size() - 1));
        mSerachTitleTextView.setHint(Utils.getTitleForSearchBar(url));
        mTabsAdapter.notifyDataSetChanged();
        setupContextMenu(webWindows.get(webWindows.size() - 1));
        Utils.Extras.xandY(mTabButton);
        if (!url.equals(HOME_PAGE_URL)) {
            facebookLog(this, "Open Url in New Tab");
        }

    }

    public void closeCurrentTab(View v) {
        int pos = (Integer) v.getTag();
        closeCurrentTab(pos);
    }

    public static boolean isPaid = false;

    private int open_counter;
    private BillingProcessor bp;
    private String REMOVE_ADS_SKU;
    /////*********************************************************ON CREATE IS HERE***********************************************************//////

    private FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    public static HashMap<String, Object> firebaseDefaultHashMap = new HashMap<>();

    private com.facebook.ads.InterstitialAd onResumeFbAd;
    private boolean isDataFetched;

    public static LinearLayout homeBannerAdContainer;
    private static com.facebook.ads.AdView facebookBannerAd;
    //    private static final String HOME_BANNER_ID = "YOUR_PLACEMENT_ID";

    void loadHomeBannerAd() {
        facebookBannerAd = new com.facebook.ads.AdView(this,
                (String) firebaseDefaultHashMap.get(Constant.HOME_BANNER_AD), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        facebookBannerAd.setAdListener(new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d(TAG, "onError: Home Page error: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        });
        facebookBannerAd.loadAd();
    }

    public static void removeHomeBannerAd() {
        homeBannerAdContainer.removeAllViews();
    }

    public static void showHomeBannerAd() {
        if (!isPaid) {
            Log.d(TAG, "showHomeBannerAd: not paid: ");
            homeBannerAdContainer.removeAllViews();
            homeBannerAdContainer.addView(facebookBannerAd);

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adContainerLinearLayout = findViewById(R.id.rl_tab_ad_container);
        adContainerLinearLayout.setVisibility(View.GONE);
        setUpFirebaseDefaultHashMap();
        loadHomeBannerAd();


        isDataFetched = false;
        //Open Counter System
        open_counter = Preference.getOpenState(Constants.OPEN_SAVER_PREF, this) + 1;
        Preference.saveOpenState(Constants.OPEN_SAVER_PREF, open_counter, this);

        if (open_counter == 1
        ) {
            FirebaseNFaceboonConsent firebaseNFaceboonConsent = new FirebaseNFaceboonConsent(this);
            firebaseNFaceboonConsent.show();
        }

        if (open_counter % 7 == 0 && !Preference.isRated(Constant.SAVE_RATE_STATE, this)) {
            new AlertDialog.Builder(this).setPositiveButton(getString(R.string.let_go), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(MainActivity.this, FirstRateActivity.class));
                }
            }).setNegativeButton(getString(R.string.not_now), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setTitle(getString(R.string.help_us))
                    .setMessage(getString(R.string.help_us_desc)).show();
        }


        REMOVE_ADS_SKU = getResources().getString(R.string.remove_ads_sku);
        bp = new BillingProcessor(this, getResources().getString(R.string.base64), this);

        restoreFullScreenSettings();

        //RemoteConfig For Promoted App

        ButterKnife.bind(this);
        newTabButton();
//        mIconImage.setImageResource(R.drawable.app_icon_browse_simply);
        mSmartMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookLog(MainActivity.this, "Bottom Sheet displayed");
                showBottomSheet();
            }
        });
        homeBannerAdContainer = findViewById(R.id.banner_container);
        checkPayState();

        mActivity = this;
        webWindows = new Vector<>();

        progress = findViewById(R.id.mainProgressBar);
        progress.setMax(100);
        progress.setScaleY(3f);

        onLonkClickMenuItems();

        mTabsAdapter = new TabsAdapter(this);
        mTabsListView.setAdapter(mTabsAdapter);

        //clipboard
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        //for resfresh search bar
        this.mHandler = new Handler();
        this.mHandler.postDelayed(m_Runnable, 5000);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

//        mInput.selectAll();
//        mInput.setSelectAllOnFocus(true);

        initSwipteToDismissTab();

        back.setColorFilter(Color.LTGRAY);
        forward.setColorFilter(Color.LTGRAY);

        initOnSearch();
        initSearchBar();
        searchBarRefresh();
        initTabListViewClick();
        callAsynchronousTask();
        loadLinksIntent();

        PERMISSIONS2 = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        setUpFirebaseRemoteConfig();
        facebookLog(this, "App Opened");
        if (!isAnythingPurchased() && open_counter % 11 == 0) {
            requestCoffeeAlertDialog();
        }


    }

    private boolean isAnythingPurchased() {
        if (bp.isInitialized()) {
            if (bp.isPurchased(REMOVE_ADS_SKU) || bp.isPurchased(getString(R.string.coffee_2)) || bp
                    .isPurchased(getString(R.string.coffee_5)) || bp.isPurchased(
                    getString(R.string.coffee_10)) || bp.isPurchased(getString(R.string.coffee_20))
            ) {
                //This means app is paid, don't show any ad
                return true;
            }
            return false;
        }
        //App is free, treat as you please
        return false;

    }

    private void checkPayState() {
//        isPaid = false;
        isPaid = isAnythingPurchased();
        if (isPaid) {
            removeHomeBannerAd();
            removeTabsBannerAd();
        } else {
            showHomeBannerAd();
        }

    }

    private void requestCoffeeAlertDialog() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.donation_title)).setMessage(
                getString(R.string.donation_desc))
                .setPositiveButton(getString(R.string.help_him), new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new CoffeeDialog(MainActivity.this).show();
                    }
                }).setNegativeButton(getString(R.string.let_him_die), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
        facebookLog(this, "Request Coffee Alert Dialog shown");
    }

    private void setUpFirebaseDefaultHashMap() {
        firebaseDefaultHashMap.put(Constant.SHOW_PROMOTED_APP, "false"); //8
        firebaseDefaultHashMap
                .put(Constant.PROMOTED_APP_PACKAGE_NAME, ""); //2
        firebaseDefaultHashMap.put(Constant.PROMOTED_APP_ICON, ""); //1
        firebaseDefaultHashMap.put(Constant.PROMOTED_APP_DESC,
                ""); //4
        firebaseDefaultHashMap.put(Constant.PROMOTED_APP_NAME, ""); //3

        firebaseDefaultHashMap.put(Constant.ON_RESUME_INT_AD_UNIT, "");  //6
        firebaseDefaultHashMap.put(Constant.TABS_BANNER_AD_ID, ""); //7
        firebaseDefaultHashMap.put(Constant.SMART_MENU_BANNER_AD, ""); //7
        firebaseDefaultHashMap.put(Constant.DOWNLOAD_BANNER_AD, "");
        firebaseDefaultHashMap.put(Constant.HOME_BANNER_AD, "");
    }

    private void setUpFirebaseRemoteConfig() {
        mFirebaseRemoteConfig.setConfigSettings(
                new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
                        .build());

        mFirebaseRemoteConfig.setDefaults(firebaseDefaultHashMap);
        Task task = mFirebaseRemoteConfig.fetch();

        task.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    mFirebaseRemoteConfig.activateFetched();
                    isDataFetched = true;

                    firebaseDefaultHashMap.put(Constant.SHOW_PROMOTED_APP,
                            mFirebaseRemoteConfig.getString(Constant.SHOW_PROMOTED_APP));

                    firebaseDefaultHashMap.put(Constant.PROMOTED_APP_PACKAGE_NAME,
                            mFirebaseRemoteConfig.getString(Constant.PROMOTED_APP_PACKAGE_NAME));

                    firebaseDefaultHashMap.put(Constant.PROMOTED_APP_ICON,
                            mFirebaseRemoteConfig.getString(Constant.PROMOTED_APP_ICON));

                    firebaseDefaultHashMap.put(Constant.ON_RESUME_INT_AD_UNIT,
                            mFirebaseRemoteConfig.getString(Constant.ON_RESUME_INT_AD_UNIT));

                    firebaseDefaultHashMap.put(Constant.PROMOTED_APP_NAME,
                            mFirebaseRemoteConfig.getString(Constant.PROMOTED_APP_NAME));

                    firebaseDefaultHashMap.put(Constant.PROMOTED_APP_DESC,
                            mFirebaseRemoteConfig.getString(Constant.PROMOTED_APP_DESC));

                    firebaseDefaultHashMap.put(Constant.TABS_BANNER_AD_ID,
                            mFirebaseRemoteConfig.getString(Constant.TABS_BANNER_AD_ID));

                    firebaseDefaultHashMap.put(Constant.SMART_MENU_BANNER_AD,
                            mFirebaseRemoteConfig.getString(Constant.SMART_MENU_BANNER_AD));

                    firebaseDefaultHashMap.put(Constant.DOWNLOAD_BANNER_AD,
                            mFirebaseRemoteConfig.getString(Constant.DOWNLOAD_BANNER_AD));

                    firebaseDefaultHashMap.put(Constant.HOME_BANNER_AD,
                            mFirebaseRemoteConfig.getString(Constant.HOME_BANNER_AD));

                    loadHomeBannerAd();
                    loadFbOnResumeInterstitialAd();

                    showHomeBannerAd();
                }
            }
        });
    }


    private void loadFbOnResumeInterstitialAd() {
        String onResumeIntAdId = (String) firebaseDefaultHashMap.get(Constant.ON_RESUME_INT_AD_UNIT);
        Log.i("TESTING_ADS", "loadFbOnResumeInterstitialAd: " + onResumeIntAdId);
        onResumeFbAd = new com.facebook.ads.InterstitialAd(this, onResumeIntAdId);
        onResumeFbAd.loadAd();
    }

    //TODO Fix it too somewhere
    private void showAdRemoveDialogHere() {
        facebookLog(this, "Remove Ad Dialog | DownloadMainActivity");
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder
                .setTitle("Remove Ads Now!")
                .setMessage("Ads are annoying. Why not remove them from whole app for life time?\n")
                .setPositiveButton(getString(android.R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                bp.purchase(MainActivity.mActivity, REMOVE_ADS_SKU);
                            }
                        })
                .setNegativeButton(getString(android.R.string.no), null)
                .show();

    }


    public static String printKeyHash(Activity context) {
        PackageInfo packageInfo;
        String key = null;
        try {
            //getting application package name, as defined in manifest
            String packageName = context.getApplicationContext().getPackageName();

            //Retriving package info
            packageInfo = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

            Log.e("Package Name=", context.getApplicationContext().getPackageName());

            for (Signature signature : packageInfo.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                key = new String(Base64.encode(md.digest(), 0));

                // String key = new String(Base64.encodeBytes(md.digest()));
                Log.e("Key Hash=", key);
            }
        } catch (NameNotFoundException e1) {
            Log.e("Name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("No such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("Exception", e.toString());
        }

        return key;
    }

    private void restoreFullScreenSettings() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (SettingsPrefrence.getFullScreenState(FULL_SCREEN_STATE, this)) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }


    private void showBottomSheet() {

        final FixedBottomSheetDialog bottomSheetDialog = new FixedBottomSheetDialog(this);

        View bottomView = getLayoutInflater().inflate(R.layout.bottom_sheet, null);
        if (isPaid) {
            bottomView.findViewById(R.id.rl_remove_ads).setVisibility(View.GONE);
            bottomView.findViewById(R.id.rl_rate).setVisibility(View.VISIBLE);
        } else {
            bottomView.findViewById(R.id.rl_remove_ads).setVisibility(View.VISIBLE);
            bottomView.findViewById(R.id.rl_rate).setVisibility(View.GONE);

            String smartMenuBannerAd = (String) firebaseDefaultHashMap.get(Constant.SMART_MENU_BANNER_AD);
            RelativeLayout container = bottomView.findViewById(R.id.rl_smart_menu_ad_container);
            AdView adView = new AdView(this, smartMenuBannerAd, AdSize.BANNER_HEIGHT_50);
            Log.d(TAG, "showBottomSheet: bottomsheet banner ad id: " + smartMenuBannerAd);
            adView.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.d(TAG, "onError: bottom sheet: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });
            container.addView(adView);
            adView.loadAd();

        }

        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);

        bottomView.findViewById(R.id.rl_rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Copy Url clicked from Bottom sheet");
                startActivity(new Intent(MainActivity.this, FirstRateActivity.class));
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });

        bottomView.findViewById(R.id.rl_copy_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Copy Url clicked from Bottom sheet");
                copyLink("input", web.getUrl());
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });
        bottomView.findViewById(R.id.rl_forward).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Go forward was clicked from Bottom Sheet");
                if (web.canGoForward()) {
                    web.goForward();
                }
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });
        bottomView.findViewById(R.id.rl_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Share was clicked from Bottom Sheet");

                sharePage();
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });
        bottomView.findViewById(R.id.rl_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Refresh was clicked from Bottom Sheet");

                web.reload();
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });
        bottomView.findViewById(R.id.rl_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Exit was clicked from Bottom Sheet");
                finish();
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });
        bottomView.findViewById(R.id.rl_remove_ads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Remove Ads was clicked from Bottom Sheet");
                removeAds();
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });
        bottomView.findViewById(R.id.rl_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Setting Opened from Bottom sheet");
                Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
                settingIntent.putExtra("testing", firebaseDefaultHashMap);
                startActivity(settingIntent);
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });
        bottomView.findViewById(R.id.rl_downloads).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLog(MainActivity.this, "Downloads was clicked from Bottom Sheet");
                startActivity(new Intent(MainActivity.this, DownloadMainActivity.class));
                if (bottomSheetDialog.isShowing()) {
                    bottomSheetDialog.dismiss();
                }
            }
        });


        bottomSheetDialog.setContentView(bottomView);
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.setOwnerActivity(this);
        bottomSheetDialog.show();

    }

    private void sharePage() {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        String url = web.getUrl();
        String title = web.getTitle();
        share.setType("text/plain");

        share.putExtra(Intent.EXTRA_TEXT, url + "\n[" + title + "] " + getString(R.string.is_good_have_a_look));
        startActivity(share);
    }


    private void removeAds() {
        bp.purchase(this, REMOVE_ADS_SKU);
        Log.v("WAQAS", "Remove Ads was clicked");

    }

    private void newTabButton() {

        mNewTabIconMAin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNewTabOnClick();
            }
        });

        mNewTabIconMAin.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openNewTabOnClick();
                return true;
            }
        });


    }

    private void openNewTabOnClick() {
        /*New Tab was opened by clicking the plus button on Tabs layout*/
        facebookLog(this, "New Tab was added by plus button");
        removeTabsBannerAd();

        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    openURLInNewTab(HOME_PAGE_URL);
                }
            }, 300);
//            openURLInNewTab(Utils.Constant.GOOGLE);

            hideTabsView();
            if (fabVisible)
                showFAB();
            mTabButtonText.setText(String.valueOf(setNotifCount()));

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private int setNotifCount() {
        return webWindows.size();
    }

    private void initSwipteToDismissTab() {
        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(
                        mTabsListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int pos : reverseSortedPositions) {
                                    closeCurrentTab(pos);
                                }

                            }
                        });

        mTabsListView.setOnTouchListener(touchListener);

        mTabsListView.setOnScrollListener(touchListener.makeScrollListener());
    }

    private void closeCurrentTab(int pos) {

        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        if ((pos) <= (webWindows.size() - 1)) {
            if (web == webWindows.get(pos)) {
                if ((pos) > 0) {
                    mContentFrame.removeAllViews();
                    mContentFrame.addView(webWindows.get(pos - 1));
                    if (webWindows.get(pos - 1).getTitle() != null) {
                        mSerachTitleTextView
                                .setHint(Utils.getTitleForSearchBar(webWindows.get(pos - 1).getUrl()));
                    }
                } else {
                    /*When Last Tab is closed from Tabs Layout*/
                    hideTabsView();
                    mContentFrame.removeAllViews();
                    startUrl(HOME_PAGE_URL);
//                    startUrl(Utils.Constant.GOOGLE);

                }
            }
            webWindows.get(pos).onPause();
            webWindows.remove(pos);
            mTabButtonText.setText(String.valueOf(setNotifCount()));


        }
        mTabsAdapter.notifyDataSetChanged();
        facebookLog(this, "Tab Closed was called");
        Log.d("TESTING_Ads", "No of Tabs: " + setNotifCount());


        if (setNotifCount() == 1 && (mTabLayout.getVisibility() == View.INVISIBLE
                || mTabLayout.getVisibility() == View.GONE)) {
            removeTabsBannerAd();
        }


    }

//    private AdView bottomBannerAd;

    private void removeTabsBannerAd() {
        if (adContainerLinearLayout.getVisibility() == View.VISIBLE) {
            adContainerLinearLayout.setVisibility(View.GONE);
        }
    }

    public static void homeTab() {
        MainActivity.mActivity.findViewById(R.id.custom_home_page).setVisibility(View.VISIBLE);
        TextView mSearchTitleTextView = MainActivity.mActivity.findViewById(R.id.searchViewTextTitle);
        mSearchTitleTextView.setText(MainActivity.mActivity.getString(R.string.search_anything));
        showHomeBannerAd();

    }

    public static void otherTab(@Nullable Integer pos) {
        MainActivity.mActivity.findViewById(R.id.custom_home_page).setVisibility(View.GONE);
        if (pos != null) {
            TextView mSerachTitleTextView = MainActivity.mActivity.findViewById(R.id.searchViewTextTitle);
            mSerachTitleTextView.setText(Utils.getTitleForSearchBar(webWindows.get(pos).getUrl()));
        }
        removeHomeBannerAd();

    }

    private void initTabListViewClick() {
        mTabsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, final int pos, long arg3) {
                /*When a tab is clicked from Tabs Layout*/
                facebookLog(MainActivity.this, "Any Tab is clicked");
                removeTabsBannerAd();
                if (pos != webWindows.size()) {
                    imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                    if (mInput != null) {
                        mInput.clearFocus();
                    }
                    hideTabsView();
                    if (webWindows.get(pos).isFabVisible()) {
                        showFAB();
                    }

                    mContentFrame.removeAllViews();
                    mContentFrame.addView(webWindows.get(pos));

                    Log.d(TAG, pos + ": " + webWindows.get(pos).getUrl());
                    if (webWindows.get(pos).getUrl().equals(HOME_PAGE_URL)) {
                        //Opened Tab is HOME TAB
                        homeTab();


                    } else {
                        //It was an other tab that is clicked
                        otherTab(pos);


                    }

                    mTabButtonText.setText(String.valueOf(setNotifCount()));

                    mTabsAdapter.notifyDataSetChanged();

                }

            }
        });
    }

    private void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);

                        searchBarRefresh();

                        try {
                            mSerachTitleTextView.setHint(Utils.getTitleForSearchBar(web.getUrl()));
                            mTabButtonText.setText(String.valueOf(setNotifCount()));


                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        mTabsAdapter.notifyDataSetChanged();
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 500); //execute in every 50000 ms


    }

    private void searchBarRefresh() {
        m_Runnable = new Runnable() {
            public void run() {
                mTabsAdapter.notifyDataSetChanged();
                MainActivity.this.mHandler.postDelayed(m_Runnable, 1000);
            }

        };

    }

    private void copyLink(String label, String url) {
        mClipData = ClipData.newPlainText(label, url);
        mClipboardManager.setPrimaryClip(mClipData);
        Utils.msg(getString(R.string.copied), MainActivity.mActivity);
    }

    private void initSearchBar() {
        Log.i(TAG, "initOnSearch Bar was called: ");
        mInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                if (hasFocus) {
                    responses = new Vector<>(0);

                    seachSugessions();
                    mInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                                int arg2, long arg3) {
                            imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                            performSearch();
                            mInput.clearFocus();
                        }
                    });
                    CustomWebView web = mContentFrame.findViewById(R.id.mywebview);

                    if (web.getUrl() != null) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            mInput.setText(
                                    Html.fromHtml(Utils.Extras.urlWrapper(web.getUrl()),
                                            Html.FROM_HTML_MODE_LEGACY),
                                    EditText.BufferType.SPANNABLE);
                        } else {
                            mInput.setText(Html.fromHtml(Utils.Extras.urlWrapper(web.getUrl())),
                                    EditText.BufferType.SPANNABLE);
                        }
                    }
                    mInput.selectAll();
                    mInput.setSelectAllOnFocus(true);

                    if (!mInput.getText().toString().isEmpty() || mInput != null) {

                        mclearAll.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mInput.setText("");

                            }
                        });
                    }
                    Log.d(TAG, "has focus past is executed");
                } else {
                    mSearchViewNormal.setVisibility(View.VISIBLE);
                    mSearchViewFocus.setVisibility(View.INVISIBLE);
                    mInput.clearFocus();
                    Log.d(TAG, "OnSearch Bar Else Part is executed");

                    try {
                        final CustomWebView web = mContentFrame.findViewById(R.id.mywebview);

                        mSerachTitleTextView.setHint(Utils.getTitleForSearchBar(web.getUrl()));
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                mSerachTitleTextView.setHint(Utils.getTitleForSearchBar(web.getUrl()));

                            }
                        }, 2000);
                        mInput.selectAll();
                        mInput.setSelectAllOnFocus(true);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }


            }
        });
    }

    private void seachSugessions() {
        suggestionsAdapter = new SearchBarAdapter(MainActivity.this, 0, responses);
        mInput.setAdapter(suggestionsAdapter);
        mInput.setScrollContainer(true);
        mInput.setDropDownAnchor(R.id.serachViewFocus);
        mInput.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        mInput.setThreshold(0);


    }

    private void loadLinksIntent() {
        Intent intent = getIntent();
        Log.d("TESTING_MENU", "loadLinksIntent was called");
    /*if (intent.getAction() == Intent.ACTION_PROCESS_TEXT) {
      Log.d("TESTING_MENU", "GET ACTION = INTENT.ACTION_PROCESSS_TEXT");
      handleIntentData(Utils
          .createLinkFromPhrase(intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString(),
              this));
    }
*/
        if (intent.getBooleanExtra("IS_MENU", false)) {
            if (!intent.getStringExtra("SEARCH_VALUE").isEmpty()) {
                String dataFromMenu = intent.getCharSequenceExtra("SEARCH_VALUE").toString();
                handleIntentData(Utils.createLinkFromPhrase(dataFromMenu, this));
            }
        } else if (intent.getAction() == Intent.ACTION_WEB_SEARCH
                || intent.getAction() == Intent.ACTION_VIEW
        ) {
            String data = intent.getDataString();
            if (data != null) {
                handleIntentData(data);
            }
        } else {
            /*First page is loaded here */
            startUrl(HOME_PAGE_URL);
        }
    }


    private void handleIntentData(String data) {
        Log.d("TESTING_MENU", "Handle Intent Data was called");
        webWindows.add(new CustomWebView(MainActivity.this, null, data));
        mContentFrame.removeAllViews();
        mContentFrame.addView(webWindows.get(webWindows.size() - 1));
        mSerachTitleTextView.setHint(Utils.getTitleForSearchBar(data));
        mTabsAdapter.notifyDataSetChanged();
    }


    private void startUrl(String url) {
        webWindows.add(new CustomWebView(MainActivity.this, null, url));
        mContentFrame.removeAllViews();
        mContentFrame.addView(webWindows.get(webWindows.size() - 1));

        mTabsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


    }

    private void performSearch() {
//        destroyAd();
        // Perform action on key press
        /*Log.e("WAQAS", "isPaid: " + isPaid);
        if (!isPaid) {
            Log.e("WAQAS", "inPerformDearch is true");
            bottomBannerAd.destroy();
            bottomBannerAd.setVisibility(View.GONE);
        }*/
        mSearchViewNormal.setVisibility(View.VISIBLE);
        mSearchViewFocus.setVisibility(View.INVISIBLE);

        mMainLayout.requestFocus();
        webViewLoad();

        // mInput.setText("");
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        mSerachTitleTextView.setHint(Utils.getTitleForSearchBar(web.getUrl()));
        web.removeAllViews();

        // Check if no view has focus:
        View view = MainActivity.this.getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            responses = new Vector<>(0);
            suggestionsAdapter = new SearchBarAdapter(this, 0, responses);

            mInput.setAdapter(suggestionsAdapter);
            mInput.setScrollContainer(true);

            //   mInput.setDropDownAnchor(R.id.toolbarLayout);
            mInput.setDropDownWidth(ViewGroup.LayoutParams.MATCH_PARENT);

            mInput.setThreshold(0);

            mInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int arg2, long arg3) {
                    imm.hideSoftInputFromWindow(mInput.getWindowToken(), 0);
                    performSearch();
                    mInput.clearFocus();
                }
            });

        }
    }

    //adding search dialog and web view
    private void initOnSearch() {

        mInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                Log.d("WAQAS", "setOnEditorActionListener was called: ");
                try {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        if (mInput != null || !mInput.getText().toString().isEmpty()) {
                            performSearch();
                        }
                        handled = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return handled;
            }

        });

        mInput.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        mInput.selectAll();
        mInput.setSelectAllOnFocus(true);


    }

    //webView data
    private void webViewLoad() {
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        Utils.webViewSearch(this, web, mInput);
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        mInput.setText("");

        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);

        mMainLayout.requestFocus();


        if (mTabLayout.getVisibility() == View.VISIBLE) {
            if (fabVisible) showFAB();
            hideTabsView();
            mMainWebLayout.setVisibility(View.VISIBLE);
            removeTabsBannerAd();


        } else if (mSearchViewFocus.getVisibility() == View.VISIBLE) {
            mSearchViewNormal.setVisibility(View.VISIBLE);
            mSearchViewFocus.setVisibility(View.INVISIBLE);
        } else if (web.canGoBack() && web.getUrl() != null) {
            try {
                web.goBack();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                Utils.ClearingData.removeHistory(this);
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, getString(R.string.press_back_again), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onDestroy() {
        if (bp != null) {
            bp.release();
        }

        if (onResumeFbAd != null) {
            onResumeFbAd.destroy();
        }
        super.onDestroy();

        Utils.ClearingData.removeHistory(this);
//        iap.onDestroy();
        Preference.savePreferences(Utils.Constant.RATE, true, this);

        Log.d("TESTING_APP_LIFECYCLE", "OnDestroy()");

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    //save webview for orientation
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        if (web.getUrl() != null) {
            web.saveState(outState);
        }
    }

    //restore webView for orientation
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
            try {
                web.restoreState(savedInstanceState);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

//        try {
//            iap.onActivityResult(requestCode, resultCode, data);
//        } catch (IabHelper.IabAsyncInProgressException e) {
//            e.printStackTrace();
//        }
// uploading...

        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == ChromeClient.FILECHOOSER_RESULTCODE) {
            PermissionsManager.getInstance()
                    .requestPermissionsIfNecessaryForResult(MainActivity.mActivity,
                            PERMISSIONS2, new PermissionsResultAction() {
                                @Override
                                public void onGranted() {
                                    if (ChromeClient.mUploadMessage != null) {
                                        Uri result = data == null || resultCode != RESULT_OK ? null
                                                : data.getData();
                                        ChromeClient.mUploadMessage.onReceiveValue(result);
                                        ChromeClient.mUploadMessage = null;
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        if (ChromeClient.mUploadMessageLol != null) {
                                            Uri[] uris = ChromeClient.FileChooserParams.parseResult(resultCode, data);
                                            ChromeClient.mUploadMessageLol.onReceiveValue(uris);
                                            ChromeClient.mUploadMessageLol = null;
                                        } else {
                                            Uri result = data == null || resultCode != RESULT_OK ? null
                                                    : data.getData();
                                            Uri[] uriss = new Uri[1];
                                            uriss[0] = result;
                                            ChromeClient.mUploadMessageLol.onReceiveValue(uriss);
                                            ChromeClient.mUploadMessageLol = null;
                                        }
                                    }

                                }

                                @Override
                                public void onDenied(String permission) {
                                }
                            });

        }

        super.onActivityResult(requestCode, resultCode, data);


    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getAction() == Intent.ACTION_WEB_SEARCH
                || intent.getAction() == Intent.ACTION_VIEW) {
            Log.d("TESTING_MENU", "ACTION_WEB_SEARCH");
            if (intent.getDataString() != null) {
                Log.d("TESTING_MENU", "Get Data String is not null");
                int tabNumber = intent.getIntExtra("tabNumber", -1);

                if (tabNumber != -1 && tabNumber < webWindows.size()) {
                    webWindows.get(tabNumber).loadUrl(intent.getDataString());
                } else {
                    tabNumber = -1;
                }

                if (tabNumber == -1) {
                    openURLInNewTab(intent.getDataString());
                }

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        try {
            if (web.getUrl() != null) {
                web.onPause();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        mInput.clearFocus();
        Log.d("TESTING_APP_LIFECYCLE", "onPause()");


    }

    @Override
    protected void onResume() {
        restoreFullScreenSettings();
        super.onResume();
        if (!isPaid && isDataFetched) {
            if (onResumeFbAd.isAdLoaded()) {
                onResumeFbAd.show();
            }
        }

        facebookLog(this, "OnResume was Called");
        mInput.clearFocus();
        CustomWebView web = mContentFrame.findViewById(R.id.mywebview);
        try {
            if (web.getUrl() != null) {
                web.onResume();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        seachSugessions();
        for (int I = 0; I < MainActivity.webWindows.size(); I++) {
            setupContextMenu(webWindows.get(I));
        }
        Log.d("TESTING_APP_LIFECYCLE", "onResume()");

    }

    private void setupContextMenu(View web) {
        web.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenu.ContextMenuInfo menuInfo) {
                final WebView.HitTestResult result = ((WebView) v).getHitTestResult();
                int resultType = result.getType();
                final String url = result.getExtra();

                if ((resultType == WebView.HitTestResult.SRC_ANCHOR_TYPE) ||
                        (resultType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {

                    //open
                    MenuItem item = menu.add(0, Utils.Constant.CONTEXT_MENU_OPEN, 0, R.string.open);
                    menuItemOpenLink(url, item);

                    //open in new
                    item = menu
                            .add(0, Utils.Constant.CONTEXT_MENU_OPEN_IN_NEW_TAB, 0, R.string.open_in_new);
                    openLinkInNewTab(url, item);

                    //copy
                    item = menu.add(0, Utils.Constant.CONTEXT_MENU_COPY, 0, R.string.copy);
                    copyItemLink(url, item);

                    //share
                    item = menu.add(0, Utils.Constant.CONTEXT_MENU_SHARE, 0, R.string.Share);
                    Utils.shareItemLink(url, item, MainActivity.this);

                    menu.setHeaderTitle(result.getExtra());

                } else if (resultType == WebView.HitTestResult.IMAGE_TYPE) {

                    //open image
                    MenuItem item = menu
                            .add(0, Utils.Constant.CONTEXT_MENU_OPEN_IN_NEW_TAB, 0, R.string.view);
                    openLinkInNewTab(url, item);

                    //open in new
                    item = menu
                            .add(0, Utils.Constant.CONTEXT_MENU_OPEN_IN_NEW_TAB, 0, R.string.open_in_new);
                    openLinkInNewTab(url, item);

                    //copy
                    item = menu.add(0, Utils.Constant.CONTEXT_MENU_COPY, 0, R.string.copy);
                    copyItemLink(url, item);

                    //share
                    item = menu.add(0, Utils.Constant.CONTEXT_MENU_SHARE, 0, R.string.Share);
                    Utils.shareItemLink(url, item, MainActivity.this);

                    //save
                    item = menu.add(0, Utils.Constant.CONTEXT_MENU_DOWNLOAD, 0, R.string.save);
                    menuItemClick(url, item);

                    menu.setHeaderTitle(result.getExtra());

                }
            }
        });
    }


    private void initiateDownload(String URL) {
        Intent i = new Intent(MainActivity.mActivity, AddDownloadActivity.class);
        i.putExtra(Intent.EXTRA_TEXT, URL);
        MainActivity.mActivity.startActivity(i);

    }

    private void menuItemClick(final String url, MenuItem item) {

        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                initiateDownload(url);
                return true;
            }
        });
    }

    private void openLinkInNewTab(final String url, MenuItem item) {
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openURLInNewTab(url);
                return true;
            }
        });
    }

    private void menuItemOpenLink(final String url, MenuItem item) {
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CustomWebView web = mContentFrame.findViewById(R.id.mywebview);

                web.loadUrl(url);
                return true;
            }
        });
    }

    private void copyItemLink(final String url, MenuItem item) {
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                copyLink("input", url);
                return true;
            }
        });
    }

    private void showProductPurchasedDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getString(R.string.purchased_title))
                .setMessage(
                        getString(R.string.purchased_desc))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })

                .show();
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        showProductPurchasedDialog();
        isPaid = true;
        removeTabsBannerAd();
        removeHomeBannerAd();

    }

    @Override
    public void onPurchaseHistoryRestored() {
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
    }

    @Override
    public void onBillingInitialized() {
        checkPayState();

    }


}

/**
 * TODO Things to implement in future
 * When user taps send email, get the complete device information to be sent via email
 * Implement Firebase Performance SDK
 * Perform the proper Facebook Analytics
 * Activity Incognito Mode for keyboard as well
 * Handle app when there is no internet connection
 * The home page for the first time is updating only when the next page is 80% downloaded. {@link com.androidbull.incognito.browser.views.ChromeClient#onProgressChanged(android.webkit.WebView, int)}  }
 * Fix the swipe to dismiss tabs listener
 * Show the Tabs through BottomSheet
 */
