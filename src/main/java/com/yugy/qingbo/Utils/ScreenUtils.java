package com.yugy.qingbo.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by yugy on 13-10-4.
 */
public class ScreenUtils {

    public static int dp(Context context, float dp){
        Resources resources = context.getResources();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return px;
    }

    public static double px(Context context, int px){
        Resources resources = context.getResources();
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, resources.getDisplayMetrics());
        return dp;
    }

}
