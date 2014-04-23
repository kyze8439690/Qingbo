package me.yugy.qingbo.listener;

import android.app.Activity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;

import me.yugy.qingbo.R;

/**
 * Created by yugy on 2014/4/21.
 */
public class DrawerToggle extends ActionBarDrawerToggle{

    public DrawerToggle(Activity activity, DrawerLayout drawerLayout) {
        super(activity, drawerLayout, R.drawable.ic_drawer, R.string.open_drawer, R.string.close_drawer);
    }

}
