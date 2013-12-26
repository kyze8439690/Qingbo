package com.yugy.qingbo.ui.componnet.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yugy.qingbo.ui.activity.MainActivity;
import com.yugy.qingbo.ui.view.TimeLineListItem;

/**
 * Created by yugy on 13-12-26.
 */
public class TimeLineListAdapter extends BaseAdapter {

    private MainActivity mActivity;

    public TimeLineListAdapter(MainActivity activity){
        mActivity = activity;
    }


    @Override
    public int getCount() {
        return mActivity.getTimeLineModels().size();
    }

    @Override
    public Object getItem(int position) {
        TimeLineListItem item = new TimeLineListItem(mActivity);
        item.parse(mActivity.getTimeLineModels().get(position));
        return item;
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
        item.parse(mActivity.getTimeLineModels().get(position));
        if(position == getCount() - 1){
            mActivity.getOldData();
        }
        return item;
    }
}
