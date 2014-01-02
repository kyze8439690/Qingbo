package com.yugy.qingbo.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yugy.qingbo.R;
import com.yugy.qingbo.model.RepostModel;

/**
 * Created by yugy on 13-12-30.
 */
public class RepostListItem extends RelativeLayout implements View.OnClickListener{
    public RepostListItem(Context context) {
        super(context);
        init();
    }

    public RepostListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RepostListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private HeadIconImageView mHead;
    private TextView mName;
    private TextView mTime;
    private TextView mText;

    private void init(){
        inflate(getContext(), R.layout.widget_comment_listitem, this);
        mHead = (HeadIconImageView) findViewById(R.id.comment_head);
        mName = (TextView) findViewById(R.id.comment_name);
        mTime = (TextView) findViewById(R.id.comment_time);
        mText = (TextView) findViewById(R.id.comment_text);
        mHead.setOnClickListener(this);
    }

    private static final DisplayImageOptions mOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(600))
            .build();

    public void parse(RepostModel data){
        mName.setText(data.name);
        mTime.setText(data.time);
        mText.setText(data.text);
        ImageLoader.getInstance().displayImage(data.head, mHead, mOptions);
    }

    @Override
    public void onClick(View v) {

    }
}
