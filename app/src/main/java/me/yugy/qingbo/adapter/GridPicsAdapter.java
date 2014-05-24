package me.yugy.qingbo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import me.yugy.qingbo.utils.ScreenUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.image.GifIconImageView;

/**
 * Created by yugy on 2014/4/17.
 */
public class GridPicsAdapter extends BaseAdapter{

    private Context mContext;
    private String[] mPics;
    private static int mImageWidth = 0;
    private boolean mIsWifi;

    public GridPicsAdapter(Context context, String[] pics, boolean isWifi){
        mContext = context;
        mPics = pics;
        mIsWifi = isWifi;
        if(mImageWidth == 0) {
            mImageWidth = ScreenUtils.dp(context, 80);
        }
    }

    @Override
    public int getCount() {
        return mPics.length;
    }

    @Override
    public Object getItem(int position) {
        return mPics[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GifIconImageView image = (GifIconImageView) convertView;
        if(image == null){
            image = new GifIconImageView(parent.getContext());
            image.setLayoutParams(new AbsListView.LayoutParams(mImageWidth, mImageWidth));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        image.setGif(TextUtils.isGifLink(mPics[position]));
        if(!mIsWifi || TextUtils.isGifLink(mPics[position])){
            ImageLoader.getInstance().displayImage(mPics[position], image);
        }else {
            ImageLoader.getInstance().displayImage(mPics[position].replace("thumbnail", "bmiddle"), image);
        }
        return image;
    }

}
