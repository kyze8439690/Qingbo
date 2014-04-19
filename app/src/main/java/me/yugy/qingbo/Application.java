package me.yugy.qingbo;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * Created by yugy on 2014/3/28.
 */
public class Application extends android.app.Application{

    private static Application sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = this;
        initImageLoader();
    }

    private void initImageLoader() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.ic_image_loading)
                .showImageOnFail(R.drawable.ic_image_fail)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new FadeInBitmapDisplayer(200))
                .build();

        ImageLoaderConfiguration.Builder configBuilder = new ImageLoaderConfiguration.Builder(sContext)
//                .writeDebugLogs()
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .denyCacheImageMultipleSizesInMemory()
                .discCache(new UnlimitedDiscCache(StorageUtils.getCacheDirectory(this)))
                .defaultDisplayImageOptions(options);

        ImageLoader.getInstance().init(configBuilder.build());
    }

    public static Context getContext(){
        return sContext;
    }
}
