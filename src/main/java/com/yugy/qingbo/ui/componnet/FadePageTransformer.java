package com.yugy.qingbo.ui.componnet;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by yugy on 13-12-27.
 */
public class FadePageTransformer implements ViewPager.PageTransformer {
    @Override
    public void transformPage(View view, float v) {
        view.setAlpha(Math.abs(Math.abs(v) - 1));
    }
}
