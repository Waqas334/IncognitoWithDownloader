
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

import static com.androidbull.incognito.browser.others.Constants.SEARCH_ENGINE;

public class SearchEngineDialog extends Dialog {
    private RadioButton google, bing, yahoo, aol, duckduckgo;
    private Context context;

    public SearchEngineDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search_engine);
        RadioGroup userAgentRadioGroup = findViewById(R.id.userAgentRadioGroup);
        userAgentRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                setUserAgent(radioGroup);
            }
        });
        google = findViewById(R.id.google);
        bing = findViewById(R.id.bing);
        yahoo = findViewById(R.id.yahoo);
        aol = findViewById(R.id.aol);
        duckduckgo = findViewById(R.id.duckduckgo);
        loadSearchEngine();
    }

    private void loadSearchEngine() {
        String searchEngine = SettingsPrefrence.getSearchEngine(SEARCH_ENGINE, context);
        if (searchEngine.equals(context.getString(R.string.defaultSearchEngine)))
            google.setChecked(true);
        else if (searchEngine.equals(context.getString(R.string.bing))) bing.setChecked(true);
        else if (searchEngine.equals(context.getString(R.string.yahoo))) yahoo.setChecked(true);
        else if (searchEngine.equals(context.getString(R.string.aol))) aol.setChecked(true);
        else if (searchEngine.equals(context.getString(R.string.duckduckgo)))
            duckduckgo.setChecked(true);
        else google.setChecked(true);
    }

    private void setUserAgent(RadioGroup radioGroup) {
        int ID = radioGroup.getCheckedRadioButtonId();
        switch (ID) {
            case R.id.google:
                SettingsPrefrence.saveSearchEngine(SEARCH_ENGINE, context.getResources().getString(R.string.defaultSearchEngine), context);
                break;
            case R.id.bing:
                SettingsPrefrence.saveSearchEngine(SEARCH_ENGINE, context.getResources().getString(R.string.bing), context);
                break;
            case R.id.yahoo:
                SettingsPrefrence.saveSearchEngine(SEARCH_ENGINE, context.getResources().getString(R.string.yahoo), context);
                break;
            case R.id.aol:
                SettingsPrefrence.saveSearchEngine(SEARCH_ENGINE, context.getResources().getString(R.string.aol), context);
                break;
            case R.id.duckduckgo:
                SettingsPrefrence.saveSearchEngine(SEARCH_ENGINE, context.getResources().getString(R.string.duckduckgo), context);
                break;
            default:
                SettingsPrefrence.saveSearchEngine(SEARCH_ENGINE, context.getResources().getString(R.string.defaultSearchEngine), context);
                break;

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.dismiss();
    }
}
