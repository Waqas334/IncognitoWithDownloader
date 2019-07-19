/*
 * Copyright (C) 2019 Tachibana General Laboratories, LLC
 * Copyright (C) 2019 Yaroslav Pronin <proninyaroslav@mail.ru>
 *
 * This file is part of Download Navi.
 *
 * Download Navi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Download Navi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Download Navi.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.androidbull.incognito.browser.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.core.utils.Utils;
import com.androidbull.incognito.browser.viewmodel.settings.SettingsViewModel;

import static com.androidbull.incognito.browser.others.Constants.FULL_SCREEN_STATE;

public class DownloadSettingsActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = DownloadSettingsActivity.class.getSimpleName();

    private Toolbar toolbar;
    private TextView detailTitle;
    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.getSettingsTheme(getApplicationContext()));
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(SettingsViewModel.class);

        setContentView(R.layout.activity_settings_download);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.settings));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        detailTitle = findViewById(R.id.detail_title);
        viewModel.detailTitleChanged.observe(this, title -> {
            if (title != null && detailTitle != null)
                detailTitle.setText(title);
        });
        restoreFullScreenSettings();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return true;
    }
}
