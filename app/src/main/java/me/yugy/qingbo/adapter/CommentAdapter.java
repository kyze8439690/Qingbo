package me.yugy.qingbo.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import me.yugy.qingbo.R;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.type.Comment;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.view.text.LinkTextView;
import me.yugy.qingbo.view.text.RelativeTimeTextView;

/**
 * Created by yugy on 2014/4/21.
 */
public class CommentAdapter extends CursorAdapter{

    private OnLoadMoreListener mOnLoadMoreListener;

    public CommentAdapter(Context context) {
        super(context, null, false);
        try {
            mOnLoadMoreListener = (OnLoadMoreListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException("Activity should implement the OnLoadMoreListener interface.");
        }
    }

    private static final DisplayImageOptions HEAD_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(200))
            .build();

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_comment_item, parent, false);
        ViewHolder viewHolder =  new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Comment comment = Comment.fromCursor(cursor);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        ImageLoader.getInstance().displayImage(comment.user.avatar, viewHolder.head, HEAD_OPTIONS);
        viewHolder.name.setText(comment.user.screenName);
        viewHolder.time.setReferenceTime(comment.time);
        viewHolder.text.setText(comment.text);

        if(cursor.getPosition() == getCount() - 1){
            DebugUtils.log("load more");
            mOnLoadMoreListener.onLoadMore();
        }
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
