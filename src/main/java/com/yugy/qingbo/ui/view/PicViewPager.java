package com.yugy.qingbo.ui.view;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.yugy.qingbo.ui.componnet.DepthPageTransformer;
import com.yugy.qingbo.ui.componnet.FadePageTransformer;
import com.yugy.qingbo.ui.componnet.ZoomOutPageTransformer;
import com.yugy.qingbo.ui.fragment.SettingsFragment;

/**
 * Created by yugy on 13-11-8.
 */
public class PicViewPager extends ViewPager{
    public PicViewPager(Context context) {
        super(context);
        init();
    }

    public PicViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setOffscreenPageLimit(3);
        String animation = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(SettingsFragment.KEY_PREF_PAGE_ANIMATOIN, "zoom");
        if(animation.equals("zoom")){
            setPageTransformer(true, new ZoomOutPageTransformer());
        }else if(animation.equals("depth")){
            setPageTransformer(true, new DepthPageTransformer());
        }else if(animation.equals("fade")){
            setPageTransformer(true, new FadePageTransformer());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}
