
package com.androidbull.incognito.browser.dialgos;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.settings.SettingsPrefrence;

import static com.androidbull.incognito.browser.others.Constants.USER_AGENT;

public class UserAgentDialog extends Dialog {
    private RadioButton androidBrowser, chrome, operaMobile, internetExplorer, safari, defaultUserAgent;
    private Context context;

    public UserAgentDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_user_agent);
        RadioGroup userAgentRadioGroup = findViewById(R.id.userAgentRadioGroup);
        userAgentRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                setUserAgent(radioGroup);
            }
        });
        androidBrowser = findViewById(R.id.androidBrowser);
        chrome = findViewById(R.id.chrome);
        operaMobile = findViewById(R.id.operaMobile);
        internetExplorer = findViewById(R.id.internetExplorer);
        safari = findViewById(R.id.safari);
        defaultUserAgent = findViewById(R.id.defaultAgent);
        loadUserAgent();


    }

    private void loadUserAgent() {
        String AGENT = SettingsPrefrence.getUserAgent(USER_AGENT, context);
        if (AGENT.equals(context.getString(R.string.androidBrowserOnWin7))) {
            androidBrowser.setChecked(true);
        } else if (AGENT.equals(context.getString(R.string.chromeAndroid))) {
            chrome.setChecked(true);
        } else if (AGENT.equals(context.getString(R.string.operaMobile))) {
            operaMobile.setChecked(true);
        } else if (AGENT.equals(context.getString(R.string.internetExplorer))) {
            internetExplorer.setChecked(true);
        } else if (AGENT.equals(context.getString(R.string.safariIos))) {
            safari.setChecked(true);
        } else {
            defaultUserAgent.setChecked(true);
        }
    }

    private void setUserAgent(RadioGroup radioGroup) {
        int ID = radioGroup.getCheckedRadioButtonId();
        switch (ID) {
            case R.id.androidBrowser:
                SettingsPrefrence.saveUserAgent(USER_AGENT, context.getResources().getString(R.string.androidBrowserOnWin7), context);
                break;
            case R.id.chrome:
                SettingsPrefrence.saveUserAgent(USER_AGENT, context.getResources().getString(R.string.chromeAndroid), context);
                break;
            case R.id.operaMobile:
                SettingsPrefrence.saveUserAgent(USER_AGENT, context.getResources().getString(R.string.operaMobile), context);
                break;
            case R.id.internetExplorer:
                SettingsPrefrence.saveUserAgent(USER_AGENT, context.getResources().getString(R.string.internetExplorer), context);
                break;
            case R.id.safari:
                SettingsPrefrence.saveUserAgent(USER_AGENT, context.getResources().getString(R.string.safariIos), context);
                break;
            case R.id.defaultAgent:
                SettingsPrefrence.saveUserAgent(USER_AGENT, context.getResources().getString(R.string.defaultUserAgent), context);
                break;
            default:
                SettingsPrefrence.saveUserAgent(USER_AGENT, context.getResources().getString(R.string.defaultUserAgent), context);
                break;

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.dismiss();
    }
}
