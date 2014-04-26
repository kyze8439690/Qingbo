package me.yugy.qingbo.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;

import me.yugy.qingbo.R;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.type.Comment;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.view.text.LinkTextView;
import me.yugy.qingbo.view.text.RelativeTimeTextView;

/**
 * Created by yugy on 2014/4/25.
 */
public class RepostCommentAdapter extends BaseAdapter{

    private ArrayList<Comment> mComments;
    private Context mContext;
    private OnLoadMoreListener mOnLoadMoreListener;

    private static final DisplayImageOptions HEAD_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(200))
            .build();

    public RepostCommentAdapter(Context context){
        mContext = context;
        mComments = new ArrayList<Comment>();
        try {
            mOnLoadMoreListener = (OnLoadMoreListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException("Activity should implement the OnLoadMoreListener interface.");
        }
    }

    @Override
    public int getCount() {
        return mComments.size();
    }

    @Override
    public Comment getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        if(view == null){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_comment_item, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        ImageLoader.getInstance().displayImage(getItem(position).user.avatar, viewHolder.head, HEAD_OPTIONS);
        viewHolder.name.setText(getItem(position).user.screenName);
        viewHolder.time.setReferenceTime(getItem(position).time);
        viewHolder.text.setText(getItem(position).text);

        if(position == getCount() - 1){
            DebugUtils.log("load more");
            mOnLoadMoreListener.onLoadMore();
        }
        return view;
    }

    /**
     * append new data into adapter and automatically call notifyDataSetChanged().
     * @param comments data to append
     */
    public void appendNewData(ArrayList<Comment> comments){
        mComments.addAll(0, comments);
        notifyDataSetChanged();
    }

    public void appendNewData(Comment comment){
        mComments.add(0, comment);
        notifyDataSetChanged();
    }

    /**
     * append old data into adapter and automatically call notifyDataSetChanged().
     * @param comments data to append
     */
    public void appendOldData(ArrayList<Comment> comments){
        for(int i = 0; i < comments.size(); i++){
            mComments.add(mComments.size(), comments.get(i));
        }
        notifyDataSetChanged();
    }

    private class ViewHolder{
        public RoundedImageView head;
        public TextView name;
        public RelativeTimeTextView time;
        public LinkTextView text;

        public ViewHolder(View view){
            head = (RoundedImageView) view.findViewById(R.id.comment_listitem_head);
            name = (TextView) view.findViewById(R.id.comment_listitem_name);
            time = (RelativeTimeTextView) view.findViewById(R.id.comment_listitem_time);
            text = (LinkTextView) view.findViewById(R.id.comment_listitem_text);
        }
    }
}
