package com.androidbull.incognito.browser;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

/*
 * This class is creating BottomSheet with android default architecture.
 *
 * */

public class FixedBottomSheetDialog extends BottomSheetDialog {

    public FixedBottomSheetDialog(@NonNull Context context) {
        super(context);
    }


    private static final String TAG = "FixedBottomSheetDialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int screenHeight = getScreenHeight(getOwnerActivity());
        int statusBarHeight = getStatusBarHeight(getOwnerActivity());

        int dialogHeight = screenHeight - statusBarHeight;
        if (getWindow() != null)
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight);
    }

    private int getStatusBarHeight(Activity activity) {
        Rect rectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        Log.d(TAG, "onCreate: Ractange TOP: " + rectangle.top);
        Log.d(TAG, "onCreate: Rectangle Bottom: " + rectangle.bottom);
        Log.d(TAG, "onCreate: Ractange Left: " + rectangle.left);
        Log.d(TAG, "onCreate: Rectangle Right: " + rectangle.right);
        return rectangle.top;
    }

    private static int getScreenHeight(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
