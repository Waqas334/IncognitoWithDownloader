package com.androidbull.incognito.browser.dialgos;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.androidbull.incognito.BuildConfig;
import com.androidbull.incognito.R;

public class AboutDialog extends DialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_about, container, false);
        TextView versionTv = v.findViewById(R.id.about_version);
        versionTv.setText(BuildConfig.VERSION_NAME);
        TextView aboutTv = v.findViewById(R.id.about_description);
        aboutTv.setText(Html.fromHtml(getString(R.string.about_description)));
        aboutTv.setMovementMethod(LinkMovementMethod.getInstance());

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP)
            this.getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

// Do all the stuff to initialize your custom view
        return v;
    }
}
