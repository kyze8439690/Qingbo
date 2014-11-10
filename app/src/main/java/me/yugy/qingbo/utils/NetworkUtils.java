package me.yugy.qingbo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import me.yugy.qingbo.Application;

/**
 * Created by yugy on 2014/3/29.
 */
public class NetworkUtils {

    public static boolean isWifi(){
        ConnectivityManager connectivityManager = (ConnectivityManager) Application.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }

}
