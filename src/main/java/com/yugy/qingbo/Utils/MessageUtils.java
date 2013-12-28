package com.yugy.qingbo.Utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.yugy.qingbo.R;
import com.yugy.qingbo.Conf;

/**
 * Created by yugy on 13-9-9.
 */
public class MessageUtils {

    public static void log(String str){
        if(Conf.DEBUG){
            Log.d(Conf.TAG, str);
        }
    }

    public static void log(int num){
        if(Conf.DEBUG){
            Log.d(Conf.TAG, String.valueOf(num));
        }
    }

    public static void toast(Context context, String str){
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void myToast(Context context, String str){
        Toast myToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
        myToast.setGravity(Gravity.TOP, 0, ScreenUtils.dp(context, 80));
        TextView text = new TextView(context);
        text.setText(str);
        text.setTextColor(Color.WHITE);
        text.setBackgroundResource(R.drawable.bg_toast);
        int padding = ScreenUtils.dp(context, 14);
        text.setPadding(padding, padding, padding, padding);
        myToast.setView(text);
        myToast.show();
    }

}
