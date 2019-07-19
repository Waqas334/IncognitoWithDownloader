package com.androidbull.incognito.browser.views;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.others.Utils;
import com.androidbull.incognito.browser.ui.MainActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.androidbull.incognito.browser.ui.MainActivity.HOME_PAGE_URL;

public class TabsAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private static final String TAG = "TabsAdapter";

    public TabsAdapter(Context c) {
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return MainActivity.webWindows.size() + 1;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }


    @Override
    public View getView(final int pos, View convertView, ViewGroup arg2) {
        final ViewHolder viewHolder;
        Log.d("TabsAdapter", "getView was called");
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.browser_item, null);

            viewHolder = new ViewHolder(convertView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.closeButton.setTag(pos);


        if (pos == MainActivity.webWindows.size() || pos < 0) {
            viewHolder.bg.setVisibility(View.GONE);

        } else {

            Log.d(TAG, "Pos: " + pos);
            viewHolder.layoutTab.setVisibility(View.VISIBLE);
            viewHolder.closeButton.setVisibility(View.VISIBLE);
            viewHolder.tabImage.setVisibility(View.VISIBLE);
            viewHolder.bg.setVisibility(View.VISIBLE);

            if (viewHolder.tabStatus.getText() == null || viewHolder.tabStatus.getText().toString()
                    .equals("")) {
                viewHolder.tabStatus.setText("...");
                viewHolder.tabUrl.setText("");

                viewHolder.bg.setBackgroundColor(Color.WHITE);
            } else if (MainActivity.webWindows.get(pos).getUrl() == null || MainActivity.webWindows
                    .get(pos).getUrl().startsWith("about:blank") || MainActivity.webWindows.get(pos).getUrl()
                    .equals("about:blank")) {
                viewHolder.tabStatus.setText("about:blank");
                viewHolder.layoutTab.setVisibility(View.GONE);
                viewHolder.tabUrl.setText("");
                viewHolder.bg.setBackgroundColor(Color.WHITE);
            } else if (MainActivity.webWindows.get(pos).getUrl().equals(HOME_PAGE_URL)) {
                viewHolder.tabStatus
                        .setText(MainActivity.mActivity.getResources().getString(R.string.home));
                viewHolder.tabUrl.setVisibility(View.INVISIBLE);
                viewHolder.tabImage.setBackgroundResource(R.drawable.home_tab_bg);

            } else {
                viewHolder.tabStatus.setText(MainActivity.webWindows.get(pos).getTitle());
                viewHolder.tabUrl
                        .setText(Utils.getTitleForSearchBar(MainActivity.webWindows.get(pos).getUrl()),
                                EditText.BufferType.SPANNABLE);
                viewHolder.tabImage.setImageBitmap(TabsView
                        .capture(MainActivity.webWindows.get(pos), 110, 95, false, Bitmap.Config.RGB_565));

            }

            if (pos == Utils.Extras.getTabNumber()) {

                viewHolder.tabStatus.setTypeface(null, Typeface.BOLD);
                viewHolder.tabUrl.setTypeface(null, Typeface.BOLD);

                viewHolder.tabUrl.setTextColor(Color.parseColor("#5F9FFA"));
                viewHolder.tabStatus.setTextColor(Color.parseColor("#5F9FFA"));
                viewHolder.closeButton.setColorFilter(Color.parseColor("#5F9FFA"));

            } else {
                viewHolder.tabStatus.setTypeface(null, Typeface.NORMAL);
                viewHolder.tabUrl.setTypeface(null, Typeface.NORMAL);

                viewHolder.tabUrl.setTextColor(Color.GRAY);
                viewHolder.tabStatus.setTextColor(Color.BLACK);
                viewHolder.closeButton.setColorFilter(Color.parseColor("#bf000000"));


            }
        }

        return convertView;
    }


    public class ViewHolder {

        @Bind(R.id.browsrItemColor)
        RelativeLayout bg;
        @Bind(R.id.layoutTab)
        LinearLayout layoutTab;
        @Bind(R.id.urlTextTab)
        TextView tabUrl;
        @Bind(R.id.tab_text)
        TextView tabStatus;
        @Bind(R.id.tabImage)
        ImageView tabImage;
        @Bind(R.id.close_tb_button)
        ImageView closeButton;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
