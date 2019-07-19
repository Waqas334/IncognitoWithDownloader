/*
 * Copyright (C) 2018 Tachibana General Laboratories, LLC
 * Copyright (C) 2018 Yaroslav Pronin <proninyaroslav@mail.ru>
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

import com.androidbull.incognito.browser.DownloadMainActivity;
import com.androidbull.incognito.browser.service.DownloadService;

/*
 * The receiver for actions of foreground notification, added by service.
 */

public class NotificationReceiver extends BroadcastReceiver
{
    public static final String NOTIFY_ACTION_SHUTDOWN_APP = "com.androidbull.incognito.browser.receiver.NotificationReceiver.NOTIFY_ACTION_SHUTDOWN_APP";
    public static final String NOTIFY_ACTION_PAUSE_ALL = "com.androidbull.incognito.browser.receiver.NotificationReceiver.NOTIFY_ACTION_PAUSE_ALL";
    public static final String NOTIFY_ACTION_RESUME_ALL = "com.androidbull.incognito.browser.receiver.NotificationReceiver.NOTIFY_ACTION_RESUME_ALL";
    public static final String NOTIFY_ACTION_PAUSE_RESUME = "com.androidbull.incognito.browser.receiver.NotificationReceiver.NOTIFY_ACTION_PAUSE_RESUME";
    public static final String NOTIFY_ACTION_CANCEL = "com.androidbull.incognito.browser.receiver.NotificationReceiver.NOTIFY_ACTION_CANCEL";
    public static final String NOTIFY_ACTION_REPORT_APPLYING_PARAMS_ERROR = "com.androidbull.incognito.browser.receiver.NotificationReceiver.NOTIFY_ACTION_REPORT_APPLYING_PARAMS_ERROR";
    public static final String TAG_ID = "id";
    public static final String TAG_ERR = "err";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if (action == null)
            return;
        Intent mainIntent, serviceIntent;
        switch (action) {
            /* Send action to the already running service */
            case NOTIFY_ACTION_SHUTDOWN_APP:
                mainIntent = new Intent(context.getApplicationContext(), DownloadMainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainIntent.setAction(NOTIFY_ACTION_SHUTDOWN_APP);
                context.startActivity(mainIntent);

                serviceIntent = new Intent(context.getApplicationContext(), DownloadService.class);
                serviceIntent.setAction(NOTIFY_ACTION_SHUTDOWN_APP);
                context.startService(serviceIntent);
                break;
            case NOTIFY_ACTION_PAUSE_ALL:
                serviceIntent = new Intent(context.getApplicationContext(), DownloadService.class);
                serviceIntent.setAction(NOTIFY_ACTION_PAUSE_ALL);
                context.startService(serviceIntent);
                break;
            case NOTIFY_ACTION_RESUME_ALL:
                serviceIntent = new Intent(context.getApplicationContext(), DownloadService.class);
                serviceIntent.setAction(NOTIFY_ACTION_RESUME_ALL);
                context.startService(serviceIntent);
                break;
            case NOTIFY_ACTION_PAUSE_RESUME:
                serviceIntent = new Intent(context.getApplicationContext(), DownloadService.class);
                serviceIntent.setAction(NOTIFY_ACTION_PAUSE_RESUME);
                serviceIntent.putExtra(TAG_ID, intent.getSerializableExtra(TAG_ID));
                context.startService(serviceIntent);
                break;
            case NOTIFY_ACTION_CANCEL:
                serviceIntent = new Intent(context.getApplicationContext(), DownloadService.class);
                serviceIntent.setAction(NOTIFY_ACTION_CANCEL);
                serviceIntent.putExtra(TAG_ID, intent.getSerializableExtra(TAG_ID));
                context.startService(serviceIntent);
                break;
            case NOTIFY_ACTION_REPORT_APPLYING_PARAMS_ERROR:
                Throwable e = (Throwable)intent.getSerializableExtra(TAG_ERR);
                //TODO ACRA HERE
//                if (e != null)
//                    Utils.reportError(e, null);
                break;
        }
    }
}
