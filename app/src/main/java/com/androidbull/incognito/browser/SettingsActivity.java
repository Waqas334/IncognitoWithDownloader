package com.androidbull.incognito.browser;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.Rating.FirstRateActivity;
import com.androidbull.incognito.browser.dialgos.AboutDialog;
import com.androidbull.incognito.browser.dialgos.CoffeeDialog;
import com.androidbull.incognito.browser.dialgos.SearchEngineDialog;
import com.androidbull.incognito.browser.dialgos.UserAgentDialog;
import com.androidbull.incognito.browser.others.Utils.Constant;
import com.androidbull.incognito.browser.settings.SettingsPrefrence;
import com.androidbull.incognito.browser.ui.MainActivity;
import com.androidbull.incognito.browser.views.TOAST;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.bumptech.glide.Glide;

import java.util.HashMap;

import static com.androidbull.incognito.browser.FacebookLogger.facebookLog;
import static com.androidbull.incognito.browser.others.Constants.FULL_SCREEN_STATE;
import static com.androidbull.incognito.browser.others.Constants.IMAGES_STATE;
import static com.androidbull.incognito.browser.others.Constants.JS_STATE;

@SuppressWarnings("MagicConstant")
public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener,
        BillingProcessor.IBillingHandler {

    private UserAgentDialog userAgentDialog;
    private SearchEngineDialog searchEngineDialog;
    private Toolbar toolbar;
    private CheckBox mCheckBoxJs, mCheckBoxImages, mCheckBoxFullScreen;
    private LinearLayout mRemoveAdsLinearLayout;
    private static final String TAG = "SettingsActivity";
    private BillingProcessor bp;
    boolean isPaid = false;

    private TextView mTvPromotedAppName, mTvPromotedAppDesc;
    private ImageView mIvPromotedAppIcon;
    private boolean showPromotedApp = false;
    private RelativeLayout mRlPromotedApp;
    private String promoted_app_package_name;

    private LinearLayout mLlMain;
    private HashMap<String, String> promotedAppData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mLlMain = findViewById(R.id.ll_main);
        mRlPromotedApp = findViewById(R.id.rl_promoted_app);
        mRlPromotedApp.setVisibility(View.GONE);
        promotedAppData = (HashMap) getIntent().getSerializableExtra("testing");
        promoted_app_package_name = promotedAppData.get(Constant.PROMOTED_APP_PACKAGE_NAME);
        if (promotedAppData.get(Constant.SHOW_PROMOTED_APP).toUpperCase().equals("Y")) {
            showPromotedApp = true;
        }

        if (showPromotedApp) {
            mRlPromotedApp.setVisibility(View.VISIBLE);
            mTvPromotedAppDesc = findViewById(R.id.tv_promoted_app_desc);
            mTvPromotedAppName = findViewById(R.id.tv_promoted_app_name);
            mIvPromotedAppIcon = findViewById(R.id.iv_promoted_app_icon);
        } else {
            mRlPromotedApp.setVisibility(View.GONE);
        }
        restoreFullScreenSettings();

        bp = new BillingProcessor(this, getResources().getString(R.string.base64), this);

        userAgentDialog = new UserAgentDialog(this);
        searchEngineDialog = new SearchEngineDialog(this);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getResources().getString(R.string.settings));
        toolbar.setTitleTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
            toolbar.setNavigationIcon(
                    ResourcesCompat.getDrawable(getResources(), R.drawable.bs_ic_clear, null));
//        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.bs_ic_clear));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCheckBoxJs = findViewById(R.id.checkbox_js);
        mCheckBoxImages = findViewById(R.id.checkboxImages);
        mCheckBoxFullScreen = findViewById(R.id.checkboxFullScreen);
        mRemoveAdsLinearLayout = findViewById(R.id.removeAdsLL);

        mCheckBoxJs.setOnCheckedChangeListener(this);
        mCheckBoxImages.setOnCheckedChangeListener(this);
        mCheckBoxFullScreen.setOnCheckedChangeListener(this);

        mCheckBoxJs.setChecked(SettingsPrefrence.getJSState(JS_STATE, this));
        mCheckBoxImages.setChecked(SettingsPrefrence.getJSState(IMAGES_STATE, this));
        mCheckBoxFullScreen.setChecked(SettingsPrefrence.getFullScreenState(FULL_SCREEN_STATE, this));
//        setting_bottom_banner_adview = findViewById(R.id.setting_bottom_banner_ad);
        if (!MainActivity.isPaid) {
            mRemoveAdsLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mRemoveAdsLinearLayout.setVisibility(View.GONE);
        }
        if (showPromotedApp) {
            setUpPromotedApp();
        }

    }

    private void setUpPromotedApp() {

        mTvPromotedAppName.setText(promotedAppData.get(Constant.PROMOTED_APP_NAME));
        mTvPromotedAppDesc.setText(promotedAppData.get(Constant.PROMOTED_APP_DESC));
        Glide.with(this)
                .load(promotedAppData.get(Constant.PROMOTED_APP_ICON))
                .into(mIvPromotedAppIcon);


    }

    public void userAgent(View view) {
        userAgentDialog.show();
        facebookLog(this, "UserAgent Dialog | Settings");


    }

    public void jsSettings(View view) {
        setCheckBoxState(mCheckBoxJs);
    }

    /*  if (mCheckBoxJs.isChecked())
                mCheckBoxJs.setChecked(false);
            else mCheckBoxJs.setChecked(true);*/
    public void imagesSetting(View view) {
        setCheckBoxState(mCheckBoxImages);
    }

    private void setCheckBoxState(CheckBox cb) {
        if (cb.isChecked()) {
            cb.setChecked(false);
        } else {
            cb.setChecked(true);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int ID = compoundButton.getId();
        if (ID == R.id.checkbox_js) {
            SettingsPrefrence.saveJSState(JS_STATE, b, SettingsActivity.this);
            Log.d(TAG, "JS Check box was clicked");
        } else if (ID == R.id.checkboxImages) {
            Log.d(TAG, "IMages Check box was clicked");
            SettingsPrefrence.saveImagesState(IMAGES_STATE, b, SettingsActivity.this);
        } else if (ID == R.id.checkboxFullScreen) {
            SettingsPrefrence.saveFullScreenState(FULL_SCREEN_STATE, b, this);
            WindowManager.LayoutParams attrs = getWindow().getAttributes();
            if (b) {
                attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            } else {
                attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
            }
            getWindow().setAttributes(attrs);
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        restoreFullScreenSettings();
    }


    public void fullScreen(View view) {
        setCheckBoxState(mCheckBoxFullScreen);
    }

    public void searchEngine(View view) {
        searchEngineDialog.show();
        facebookLog(this, "Search Engine | Settings");

    }

    public void removeAds(View view) {
        bp.purchase(this, getResources().getString(R.string.remove_ads_sku));
    }

    public void feedback(View view) {
        sendEmail("");
    }

    private void sendEmail(String customAddition) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("mailto:" + "andbullofficial@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    "Incognito Browser - Browse Anonymously " + customAddition);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No Application Found for Sending E-mail.", Toast.LENGTH_SHORT).show();
        }
    }

    public void rate(View view) {
        startActivity(new Intent(this, FirstRateActivity.class));
        facebookLog(this, "Rate | Settings");
    }

    public void share(View view) {
        DefaultFunctions.shareThisApp(this);
    }

    public void promotedAppClicked(View view) {
        TOAST.make(this, "Promoted App Clicked: " + promoted_app_package_name);
        facebookLog(this, "Promoted App Clicked");
        String URL = "https://play.google.com/store/apps/details?id=" + promoted_app_package_name
                + "&referrer=utm_source%3DIncognitoBrowser%26utm_medium%Setting%2520Screen%26utm_campaign%3DBottom%2520Setting%2520Incognito";
        DefaultFunctions.gotoGooglePlayPageWithUrl(this, URL);

    }


    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        isPaid = true;
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }

    private static final String TAG_ABOUT_DIALOG = "about_dialog";

    public void about(View view) {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.show(getSupportFragmentManager(), "");

    }


    public void buyCoffee(View view) {
        CoffeeDialog coffeeDialog = new CoffeeDialog(this);
        coffeeDialog.show();
    }
}
