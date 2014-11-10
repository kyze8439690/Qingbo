package me.yugy.qingbo.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import me.yugy.qingbo.R;
import me.yugy.qingbo.type.UserIndex;

/**
 * Created by yugy on 2014/6/2.
 */
public class MentionAdapter extends CursorAdapter {

    public MentionAdapter(Context context) {
        super(context, null, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.view_mention_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        UserIndex userIndex = UserIndex.fromCursor(cursor);
        ImageView head = (ImageView) view.findViewById(R.id.mention_item_head);
        ImageLoader.getInstance().displayImage(userIndex.avatar, head, HEAD_OPTIONS);
        TextView name = (TextView) view.findViewById(R.id.mention_item_name);
        name.setText(userIndex.screenName);
    }

    private static final DisplayImageOptions HEAD_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .build();
}
