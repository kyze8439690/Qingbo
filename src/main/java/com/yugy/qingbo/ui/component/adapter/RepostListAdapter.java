package com.yugy.qingbo.ui.component.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yugy.qingbo.model.RepostModel;
import com.yugy.qingbo.ui.view.RepostListItem;
import com.yugy.qingbo.utils.MessageUtils;

import java.util.ArrayList;

/**
 * Created by yugy on 13-12-30.
 */
public class RepostListAdapter extends BaseAdapter{

    private ArrayList<RepostModel> mData;
    private Context mContext;

    public RepostListAdapter(Context context){
        mContext = context;
        mData = new ArrayList<RepostModel>();
    }

    public ArrayList<RepostModel> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public RepostModel getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RepostListItem item;
        if(convertView != null){
            item = (RepostListItem) convertView;
        }else{
            MessageUtils.log("convertView is null");
            item = new RepostListItem(mContext);
        }
        item.parse(mData.get(position));
        return item;
    }
}
