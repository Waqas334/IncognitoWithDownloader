

package com.androidbull.incognito.browser.others;

import android.net.ParseException;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.util.Patterns.GOOD_IRI_CHAR;


public class WebAddress {

    private static final int MATCH_GROUP_SCHEME = 1;
    private static final int MATCH_GROUP_AUTHORITY = 2;
    private static final int MATCH_GROUP_HOST = 3;
    private static final int MATCH_GROUP_PORT = 4;
    private static final int MATCH_GROUP_PATH = 5;
    private static Pattern sAddressPattern = Pattern.compile(
            /* scheme    */ "(?:(http|https|file)\\:\\/\\/)?" +
            /* authority */ "(?:([-A-Za-z0-9$_.+!*'(),;?&=]+(?:\\:[-A-Za-z0-9$_.+!*'(),;?&=]+)?)@)?" +
            /* host      */ "([" + GOOD_IRI_CHAR + "%_-][" + GOOD_IRI_CHAR + "%_\\.-]*|\\[[0-9a-fA-F:\\.]+\\])?" +
            /* port      */ "(?:\\:([0-9]*))?" +
            /* path      */ "(\\/?[^#]*)?" +
            /* anchor    */ ".*", Pattern.CASE_INSENSITIVE);
    private String mScheme;
    private String mHost;
    private int mPort;
    private String mPath;
    private String mAuthInfo;

    /**
     * parses given uriString.
     */
    public WebAddress(String address) throws ParseException {
        if (address == null) {
            throw new NullPointerException();
        }

        // android.util.Log.d(LOGTAG, "WebAddress: " + address);

        mScheme = "";
        mHost = "";
        mPort = -1;
        mPath = "/";
        mAuthInfo = "";

        Matcher m = sAddressPattern.matcher(address);
        String t;
        if (m.matches()) {
            t = m.group(MATCH_GROUP_SCHEME);
            if (t != null) mScheme = t.toLowerCase(Locale.ROOT);
            t = m.group(MATCH_GROUP_AUTHORITY);
            if (t != null) mAuthInfo = t;
            t = m.group(MATCH_GROUP_HOST);
            if (t != null) mHost = t;
            t = m.group(MATCH_GROUP_PORT);
            if (t != null && t.length() > 0) {
                // The ':' character is not returned by the regex.
                try {
                    mPort = Integer.parseInt(t);
                } catch (NumberFormatException ex) {
                    try {
                        throw new Exception("Bad port");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            t = m.group(MATCH_GROUP_PATH);
            if (t != null && t.length() > 0) {
                /* handle busted myspace frontpage redirect with
                   missing initial "/" */
                if (t.charAt(0) == '/') {
                    mPath = t;
                } else {
                    mPath = "/" + t;
                }
            }

        } else {
            // nothing found... outta here
            try {
                throw new Exception("Bad address");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /* Get port from scheme or scheme from port, if necessary and
           possible */
        if (mPort == 443 && mScheme.equals("")) {
            mScheme = "https";
        } else if (mPort == -1) {
            if (mScheme.equals("https"))
                mPort = 443;
            else
                mPort = 80; // default
        }
        if (mScheme.equals("")) mScheme = "http";
    }

    @Override
    public String toString() {
        String port = "";
        if ((mPort != 443 && mScheme.equals("https")) ||
                (mPort != 80 && mScheme.equals("http"))) {
            port = ":" + Integer.toString(mPort);
        }
        String authInfo = "";
        if (mAuthInfo.length() > 0) {
            authInfo = mAuthInfo + "@";
        }

        return mScheme + "://" + authInfo + mHost + port + mPath;
    }

    public String getScheme() {
        return mScheme;
    }

    public void setScheme(String scheme) {
        mScheme = scheme;
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String host) {
        mHost = host;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public String getAuthInfo() {
        return mAuthInfo;
    }

    public void setAuthInfo(String authInfo) {
        mAuthInfo = authInfo;
    }

    public static String getFileNameFromHeader(String header) {
        int pos = 0;

        String fileName = null;

        if ((pos = header.toLowerCase().lastIndexOf("filename=")) >= 0) {
            fileName = header.substring(pos + 9);
            pos = fileName.lastIndexOf(";");

            if (pos > 0) {
                fileName = fileName.substring(0, pos - 1);
            }
        }
        if (fileName != null)
            fileName = fileName.replaceAll("\"", "");

        return fileName;
    }

}