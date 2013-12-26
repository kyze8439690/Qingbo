package com.yugy.qingbo.ui.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.yugy.qingbo.ui.fragment.SettingsFragment;

/**
 * Created by yugy on 13-12-26.
 */
public class SettingsActivity extends PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}
