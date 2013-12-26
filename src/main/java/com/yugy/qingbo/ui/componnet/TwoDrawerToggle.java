package com.yugy.qingbo.ui.componnet;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.yugy.qingbo.R;

/**
 * Created by yugy on 13-12-26.
 */
public class TwoDrawerToggle extends ActionBarDrawerToggle {

    private DrawerLayout mDrawerLayout;
    private View mLeftDrawer;
    private View mRightDrawer;
    private AnimationDrawable mJingleDrawable;

    public TwoDrawerToggle(Activity activity, DrawerLayout drawerLayout, View leftDrawer, View rightDrawer) {
        super(activity, drawerLayout, R.drawable.ic_drawer_toggle, R.string.app_name, R.string.app_name);
        mDrawerLayout = drawerLayout;
        mLeftDrawer = leftDrawer;
        mRightDrawer = rightDrawer;
        mJingleDrawable = (AnimationDrawable) ((TextView)rightDrawer.findViewById(R.id.main_right_drawer_emptyview)).getCompoundDrawables()[1];
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        if(drawerView.equals(mRightDrawer)){
            if(mJingleDrawable.isVisible()){
                if(mJingleDrawable.isRunning()){
                    mJingleDrawable.stop();
                }
                mJingleDrawable.start();
            }
        }else{
            super.onDrawerOpened(drawerView);
        }
        if(mDrawerLayout.isDrawerOpen(Gravity.END) && drawerView.equals(mLeftDrawer)){
            mDrawerLayout.closeDrawer(Gravity.END);
        }else if(mDrawerLayout.isDrawerOpen(Gravity.START) && drawerView.equals(mRightDrawer)){
            mDrawerLayout.closeDrawer(Gravity.START);
        }
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        if(drawerView == mLeftDrawer){
            super.onDrawerSlide(drawerView, slideOffset);
        }
    }
}