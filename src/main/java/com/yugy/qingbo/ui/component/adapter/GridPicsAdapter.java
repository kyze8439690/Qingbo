package com.yugy.qingbo.ui.component.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yugy.qingbo.ui.view.GifIconImageView;
import com.yugy.qingbo.utils.ScreenUtils;
import com.yugy.qingbo.utils.TextUtils;

import java.util.ArrayList;

/**
 * Created by yugy on 13-12-29.
 */
public class GridPicsAdapter extends BaseAdapter {

    private ArrayList<String> data;
    private Context mContext;
    private int imageWidth;

    public GridPicsAdapter(Context context, ArrayList<String> model){
        mContext = context;
        data = model;
        imageWidth = ScreenUtils.dp(context, 80);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GifIconImageView image = (GifIconImageView) convertView;
        if(image == null){
            image = new GifIconImageView(mContext);
            image.setLayoutParams(new AbsListView.LayoutParams(imageWidth, imageWidth));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        image.setGif(TextUtils.isGifLink(data.get(position)));
        ImageLoader.getInstance().displayImage(data.get(position), image);
        return image;
    }
}