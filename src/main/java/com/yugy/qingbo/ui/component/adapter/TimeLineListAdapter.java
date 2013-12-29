package com.yugy.qingbo.ui.component.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yugy.qingbo.model.TimeLineModel;
import com.yugy.qingbo.ui.activity.MainActivity;
import com.yugy.qingbo.ui.view.HeadIconImageView;
import com.yugy.qingbo.ui.view.TimeLineListItem;

import java.util.ArrayList;

/**
 * Created by yugy on 13-12-26.
 */
public class TimeLineListAdapter extends BaseAdapter {

    private MainActivity mActivity;
    private ArrayList<TimeLineModel> mData;

    public TimeLineListAdapter(MainActivity activity){
        mData = new ArrayList<TimeLineModel>();
        mActivity = activity;
    }

    public ArrayList<TimeLineModel> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public TimeLineModel getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TimeLineListItem item;
        if(convertView != null){
            item = (TimeLineListItem) convertView;
        }else{
            item = new TimeLineListItem(mActivity);
        }
        item.parse(mData.get(position));
        if(position == getCount() - 1){
            mActivity.getOldData();
        }
        return item;
    }

}
