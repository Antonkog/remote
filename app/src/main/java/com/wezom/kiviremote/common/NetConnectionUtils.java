package com.wezom.kiviremote.common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by andre on 26.05.2017.
 */

public class NetConnectionUtils {
    private NetConnectionUtils() {
    }

    public static boolean isConnectedWithWifi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.getTypeName().equals("WIFI");
    }

    public static String getCurrentSsid(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        return info.getSSID();
    }

    public static List<WifiConfiguration> getListOfSSIDs(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.getConfiguredNetworks();
    }
}
