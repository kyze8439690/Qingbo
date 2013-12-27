package com.yugy.qingbo.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.yugy.qingbo.R;
import com.yugy.qingbo.Utils.MessageUtil;
import com.yugy.qingbo.ui.activity.MainActivity;

/**
 * Created by yugy on 13-12-26.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    public static final String KEY_PREF_FONT = "pref_font";
    public static final String KEY_PREF_PAGE_ANIMATOIN = "pref_page_animation";
    public static final String KEY_PREF_TIMELINE_AMOUNT = "pref_timeline_amount";
    public static final String KEY_PREF_SCROLL_ANIMATION = "pref_scroll_animation";

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(KEY_PREF_FONT)){
            getActivity().setResult(MainActivity.RESULT_SETTING_FONT_CHANGED);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
