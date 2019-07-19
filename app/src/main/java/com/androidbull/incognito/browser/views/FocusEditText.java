package com.androidbull.incognito.browser.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;

import static com.androidbull.incognito.browser.ui.MainActivity.HOME_PAGE_URL;

public class FocusEditText extends AutoCompleteTextView {
    public FocusEditText(final Context context) {
        super(context);
    }

    public FocusEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean mHadVisibility = false;


    @Override
    protected void onFocusChanged(final boolean focused, final int direction, final Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (hasFocus()) {
            setImeVisibility(true);
            mHadVisibility = true;
            String current_url = String.valueOf(this.getText());
            if (current_url.equals(HOME_PAGE_URL)) {
                this.setText("");
            }
        } else {
            if (mHadVisibility) {
                setImeVisibility(false);
            }
            mHadVisibility = false;
        }
    }

    private void setImeVisibility(final boolean visible) {
        if (visible) {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).
                    toggleSoftInput(InputMethodManager.SHOW_FORCED,
                            InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            InputMethodManager imm = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    @Override
    public boolean onKeyPreIme(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // special case for the back key, we do not even try to send it
            // to the drop down list but instead, consume it immediately
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    clearFocus();
                    setImeVisibility(false);
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }


}
