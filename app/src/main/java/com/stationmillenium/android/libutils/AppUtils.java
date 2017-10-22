/**
 *
 */
package com.stationmillenium.android.libutils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.stationmillenium.android.BuildConfig;
import com.stationmillenium.android.services.MediaPlayerService;

/**
 * Android version utils
 *
 * @author vincent
 */
public class AppUtils {

    private static final String TAG_NETWORK_AVAILABLE = "AppUtils#isNetworkAvail";
    private static final String TAG_WIFI_ONLY_NOT_ACTIVATED = "AppUtils#isWifiOnly";

    /**
     * Return if the API level >= 21
     *
     * @return <code>true</code> if API level >= 21, <code>false</code> if not
     */
    public static boolean isAPILevel21Available() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    /**
     * Return if the API level >= 26
     *
     * @return <code>true</code> if API level >= 26, <code>false</code> if not
     */
    public static boolean isAPILevel26Available() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O);
    }

    /**
     * Return if the API level >= 26
     *
     * @return <code>true</code> if API level >= 19, <code>false</code> if not
     */
    public static boolean isAPILevel19Available() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
    }

    /**
     * Get is the network is available according to wifi only preference
     *
     * @param context the curent {@link Context}
     * @return <code>true</code> if network is available, <code>false</code> otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        //get params
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null) {
            boolean wifiOnly = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SharedPreferencesConstants.WIFI_ONLY, false);
            boolean networkUp = networkInfo.isConnected();
            boolean wifiType = (networkInfo.getType() == ConnectivityManager.TYPE_WIFI);
            boolean networkConnected;

            //check state
            if (BuildConfig.DEBUG)
                Log.d(TAG_NETWORK_AVAILABLE, "Wifi only : " + wifiOnly + " - Network up : " + networkUp + " - Wifi type : " + wifiType);
            if (wifiOnly)
                networkConnected = networkUp && wifiType;
            else
                networkConnected = networkUp;
            if (BuildConfig.DEBUG)
                Log.d(TAG_NETWORK_AVAILABLE, "Network connected : " + networkConnected);

            return networkConnected;

        } else {
            if (BuildConfig.DEBUG)
                Log.d(TAG_NETWORK_AVAILABLE, "No network available");
            return false;
        }
    }

    /**
     * Check if the wifi only param is check and wifi not activated
     *
     * @param context the curent {@link Context}
     * @return <code>true</code> if wifi only option checked and wifi not connected, <code>false</code> otherwise
     */
    public static boolean isWifiOnlyAndWifiNotConnected(Context context) {
        //get params
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean wifiOnly = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SharedPreferencesConstants.WIFI_ONLY, false);
        boolean networkUp = networkInfo.isConnected();

        //check state
        if (BuildConfig.DEBUG)
            Log.d(TAG_WIFI_ONLY_NOT_ACTIVATED, "Wifi only : " + wifiOnly + " - Network up : " + networkUp);
        return (wifiOnly && !networkUp);
    }

    /**
     * Check if {@link MediaPlayerService} is running
     *
     * @param context the curent {@link Context}
     * @return <code>true</code> if running, <code>false</code> if not
     */
    public static boolean isMediaPlayerServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MediaPlayerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
