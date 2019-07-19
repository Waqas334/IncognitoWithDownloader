package com.androidbull.incognito.browser.views;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.androidbull.incognito.R;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class SearchBarAdapter extends ArrayAdapter<String> {
    private List<String> suggestions;
    private String responseString = null;
    private Filter nameFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                if (!constraint.toString().equals("")) {


//                    HttpClient httpclient = new DefaultHttpClient();
                    HttpClient httpClient1 = new DefaultHttpClient();
                    HttpResponse response;
                    try {
                        String url = String.format("https://www.google.com/complete/search?hl=%s&client=firefox&q=%s",
                                Locale.getDefault().getCountry(),
                                URLEncoder.encode(constraint.toString(), "utf-8"));


                        response = httpClient1.execute(new HttpGet(url));
                        StatusLine statusLine = response.getStatusLine();
                        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                            responseString = EntityUtils.toString(response.getEntity(), "UTF-8");


                        } else {
                            //Closes the connection.
                            response.getEntity().getContent().close();

                            throw new IOException(statusLine.getReasonPhrase());
                        }
                    } catch (ClientProtocolException e) {
                    } catch (IOException e) {
                    }
             }

                if (responseString != null) {

                    try {
                        JSONArray jArray = new JSONArray(responseString).getJSONArray(1);


                        for (int i = 0; i < jArray.length(); i++) {
                            suggestions.add(jArray.getString(i));


                        }


                        FilterResults filterResults = new FilterResults();
                        filterResults.values = suggestions;
                        filterResults.count = suggestions.size();
                        return filterResults;
                    } catch (Exception e) {
                        return null;
                    }
                } else
                    return null;
            } else {
                return null;
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null) {
                Vector<String> filteredList = (Vector<String>) ((Vector<String>) results.values).clone();
                if (results != null && results.count > 0) {
                    clear();
                    for (String c : filteredList) {
                        add(c);
                    }
                    notifyDataSetChanged();
                }
            } else {
                clear();
                notifyDataSetInvalidated();
            }
        }
    };

    public SearchBarAdapter(Context context, int viewResourceId, List<String> items) {
        super(context, R.layout.browser_bar_suggestion_item, R.id.serachBarText);
        this.suggestions = new Vector<>();
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }



}