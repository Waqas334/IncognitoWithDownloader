package com.androidbull.incognito.browser;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;

import com.androidbull.incognito.R;


public class DefaultFunctions {

    public static void shareThisApp(Context c) {
        try {

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, c.getPackageName());
            String sAux = c.getResources().getString(R.string.share_this_app_with) + "\n";
            sAux = sAux + "https://play.google.com/store/apps/details?id=" + c.getPackageName() + "\n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            c.startActivity(Intent.createChooser(i, c.getResources().getString(R.string.choose_one)));
        } catch (Exception e) {
            //e.toString();
        }

    }

    public static void gotoGooglePlayPageWithPackageName(Context c) {
        try {
            c.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + c.getPackageName())));
        } catch (ActivityNotFoundException e) {
            c.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + c.getPackageName())));
        }
    }

    public static void gotoGooglePlayPageWithPackageName(Context c, String packageName) {
        try {
            c.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
        } catch (ActivityNotFoundException e) {
            c.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));

        }
    }

    public static void gotoGooglePlayPageWithUrl(Context c, String Url) {
        try {
            c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Url)));
        } catch (ActivityNotFoundException e) {
            c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Url)));

        }
    }

    public static boolean isPackageInstalled(Context context, String packagename) {
        try {
            context.getPackageManager().getPackageInfo(packagename, 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }


    public static void contactUs(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "andbullofficial@gmail.com", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New Incognito Browser Contact");
//    emailIntent.putExtra(Intent.EXTRA_TEXT, "Body");
        context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }


    public static void gotoGooglePlayPage(Context c) {
        try {
            c.startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + c.getPackageName())));
        } catch (ActivityNotFoundException e) {
            c.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + c.getPackageName())));
        }
    }
}
