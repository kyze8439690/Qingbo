package com.yugy.qingbo;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yugy.qingbo.R;
import com.yugy.qingbo.ui.fragment.SettingsFragment;

import java.io.File;

/**
 * Created by yugy on 13-10-29.
 */
public class MyApplication extends Application {

    private static Context mContext;

    public static Context getContext(){
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.jingles_1)
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_image_fail)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new FadeInBitmapDisplayer(600))
                .build();
        File cacheDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getPath());
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSize(2 * 1024 * 1024)
                .defaultDisplayImageOptions(options)
                .discCache(new UnlimitedDiscCache(cacheDir)).build();
        ImageLoader.getInstance().init(config);

        SettingsFragment.fontPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SettingsFragment.KEY_PREF_FONT, "default");

    }
}
