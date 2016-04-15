package hust.nursenfcclient.helps;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import hust.nursenfcclient.R;

/**
 * Created by admin on 2015/11/24.
 */
public class WifiHelper {

    //是否连接WIFI
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo.isConnected();
    }

    public static void checkAndOPenWifi(Context context) {
        if (!WifiHelper.isWifiConnected(context)) {
            Toast.makeText(context, R.string.wifi_not_open, Toast.LENGTH_SHORT).show();
            WifiHelper.openWifi(context);
        }
    }

    public static void checkAndOPenWifiWithNoHint(Context context) {
        if (!WifiHelper.isWifiConnected(context)) {
            WifiHelper.openWifi(context);
        }
    }

    public static void reverseWifiState(Context context) {
        if (!WifiHelper.isWifiConnected(context)) {
            Toast.makeText(context, R.string.wifi_not_open, Toast.LENGTH_SHORT).show();
            WifiHelper.openWifi(context);
        } else {
            Toast.makeText(context, R.string.wifi_close, Toast.LENGTH_SHORT).show();
            WifiHelper.closeWifi(context);
        }
    }


    /****
     * 打开WIFI
     ***/
    public static boolean openWifi(Context context) {
        return opclWifi(context, true);
    }

    /****
     * 关闭WIFI
     ***/
    public static boolean closeWifi(Context context) {
        return opclWifi(context, false);
    }

    /****
     * 打开关闭WIFI
     ****/
    private static boolean opclWifi(Context context, boolean type) {
        boolean result = true;
        // 获取WIFIManager实例
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        Log.i("LOG_TAG", String.valueOf(type));

        if (wifiManager.isWifiEnabled() ^ type) {
            // 打开关闭WIFI
            result = wifiManager.setWifiEnabled(type);
        }

        return result;
    }
}


