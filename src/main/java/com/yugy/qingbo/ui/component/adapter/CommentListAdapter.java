package com.yugy.qingbo.ui.component.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yugy.qingbo.model.CommentModel;
import com.yugy.qingbo.ui.view.CommentListItem;
import com.yugy.qingbo.ui.view.TimeLineListItem;
import com.yugy.qingbo.utils.MessageUtils;

import java.util.ArrayList;

/**
 * Created by yugy on 13-12-30.
 */
public class CommentListAdapter extends BaseAdapter{

    private ArrayList<CommentModel> mData;
    private Context mContext;

    public CommentListAdapter(Context context){
        mContext = context;
        mData = new ArrayList<CommentModel>();
    }

    public ArrayList<CommentModel> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public CommentModel getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommentListItem item;
        if(convertView != null){
            item = (CommentListItem) convertView;
        }else{
            MessageUtils.log("convertView is null");
            item = new CommentListItem(mContext);
        }
        item.parse(mData.get(position));
        return item;
    }
}
