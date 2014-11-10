package me.yugy.qingbo.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import me.yugy.qingbo.BuildConfig;
import me.yugy.qingbo.Conf;

/**
 * Created by yugy on 14-1-29.
 */
public class DebugUtils {

    public static void log(Object log){
        if(BuildConfig.DEBUG){
            if(log == null){ //log null
                Log.e(Conf.LOG_TAG, "log content is null");
            }else if(log instanceof Integer || log instanceof Float || log instanceof Double){ //log num
                Log.d(Conf.LOG_TAG, String.valueOf(log));
            }else if(log instanceof String){ //log string
                Log.d(Conf.LOG_TAG, (String) log);
            }else if(log instanceof JSONObject || log instanceof JSONArray){ //log json
                Log.d(Conf.LOG_TAG, log.toString());
            }else if(log instanceof byte[]){ //log byte array
                Log.d(Conf.LOG_TAG, new String((byte[]) log));
            }
        }
    }
}
