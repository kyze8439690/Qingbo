package com.yugy.qingbo.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.yugy.qingbo.R;
import com.yugy.qingbo.ui.fragment.SettingsFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by yugy on 13-12-26.
 */
public class BaseActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFont();
        setContentView(R.layout.activity_main);
        MobclickAgent.onError(this);
        UmengUpdateAgent.update(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    private void initFont(){
        if(SettingsFragment.fontPreferences.equals("default")){
            CalligraphyConfig.initDefault("");
        }else if(SettingsFragment.fontPreferences.equals("condensed")){
            CalligraphyConfig.initDefault("RobotoCondensed-Regular.ttf");
        }else if(SettingsFragment.fontPreferences.equals("slab")){
            CalligraphyConfig.initDefault("RobotoSlab-Regular.ttf");
        }
    }

}
