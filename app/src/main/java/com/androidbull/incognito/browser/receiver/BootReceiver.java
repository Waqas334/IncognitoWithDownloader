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

package com.androidbull.incognito.browser.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.MainApplication;
import com.androidbull.incognito.browser.core.DownloadEngine;
import com.androidbull.incognito.browser.settings.SettingsManager;

/*
 * The receiver for autostart stopped downloads.
 */

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction() == null)
            return;

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences pref = SettingsManager.getInstance(context.getApplicationContext()).getPreferences();
            if (pref.getBoolean(context.getString(R.string.pref_key_autostart), SettingsManager.Default.autostart)) {
                DownloadEngine engine = ((MainApplication)context.getApplicationContext()).getDownloadEngine();
                engine.restoreDownloads();
            }
        }
    }
}
