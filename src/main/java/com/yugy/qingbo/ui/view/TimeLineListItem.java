package com.yugy.qingbo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yugy.qingbo.R;
import com.yugy.qingbo.ui.component.adapter.GridPicsAdapter;
import com.yugy.qingbo.utils.NetworkUtils;
import com.yugy.qingbo.utils.TextUtils;
import com.yugy.qingbo.ui.activity.DetailActivity;
import com.yugy.qingbo.model.TimeLineModel;

/**
 * Created by yugy on 13-10-4.
 */
public class TimeLineListItem extends RelativeLayout implements View.OnClickListener, AdapterView.OnItemClickListener{
    public TimeLineListItem(Context context) {
        super(context);
        init();
    }

    private HeadIconImageView mHead;
    private TextView mName;
    private TextView mText;
    private TextView mTopic;
    private TextView mTime;
    private NoScrollGridView mGridView;
    private SelectorImageView mPic;
    private View mLine;
    private TextView mRepostName;
    private TextView mRepostText;
    private TextView mCommentCount;
    private TextView mRepostCount;

    private TimeLineModel mData;

    private void init(){
        inflate(getContext(), R.layout.widget_timeline_listitem, this);
        mHead = (HeadIconImageView) findViewById(R.id.timeline_listitem_head);
        mName = (TextView) findViewById(R.id.timeline_listitem_name);
        mText = (TextView) findViewById(R.id.timeline_listitem_text);
        mText.setMovementMethod(LinkMovementMethod.getInstance());
        mTopic = (TextView) findViewById(R.id.timeline_listitem_topic);
        mTime = (TextView) findViewById(R.id.timeline_listitem_time);
        mGridView = (NoScrollGridView) findViewById(R.id.timeline_listitem_picgrid);
        mGridView.setSelector(getResources().getDrawable(R.drawable.list_selector_holo));
        mPic = (SelectorImageView) findViewById(R.id.timeline_listitem_pic);
        mLine = findViewById(R.id.timeline_listitem_line);
        mRepostName = (TextView) findViewById(R.id.timeline_listitem_repost_name);
        mRepostName.setMovementMethod(LinkMovementMethod.getInstance());
        mRepostText = (TextView) findViewById(R.id.timeline_listitem_repost_text);
        mRepostText.setMovementMethod(LinkMovementMethod.getInstance());
        mCommentCount = (TextView) findViewById(R.id.timeline_listitem_commentcount);
        mRepostCount = (TextView) findViewById(R.id.timeline_listitem_repostcount);

        mHead.setOnClickListener(this);
        mGridView.setOnItemClickListener(this);
        mPic.setOnClickListener(this);
        mCommentCount.setOnClickListener(this);
        mRepostCount.setOnClickListener(this);
    }

    public void parse(TimeLineModel data){
        this.mData = data;
        if(data.hasPics || data.hasRepostPics){
            mPic.setVisibility(GONE);
            mGridView.setVisibility(VISIBLE);
            mGridView.setAdapter(new GridPicsAdapter(getContext(), data.pics));
            LayoutParams lp = (LayoutParams) mCommentCount.getLayoutParams();
            lp.addRule(BELOW, R.id.timeline_listitem_picgrid);
            mCommentCount.setLayoutParams(lp);
        }else{
            mGridView.setVisibility(GONE);
            LayoutParams lp = (LayoutParams) mCommentCount.getLayoutParams();
            lp.addRule(BELOW, R.id.timeline_listitem_pic);
            mCommentCount.setLayoutParams(lp);
            if(data.hasPic || data.hasRepostPic){
                mPic.setVisibility(VISIBLE);
                mPic.setGif(TextUtils.isGifLink(data.pics.get(0)));
                if(NetworkUtils.isWifi(getContext())){
                    ImageLoader.getInstance().displayImage(data.pics.get(0).replace("thumbnail", "bmiddle"), mPic);
                }else{
                    ImageLoader.getInstance().displayImage(data.pics.get(0), mPic);
                }
            }else{
                mPic.setVisibility(GONE);
            }
        }
        if(data.topics.size() == 0){
            mTopic.setVisibility(INVISIBLE);
        }else{
            mTopic.setVisibility(VISIBLE);
            mTopic.setText(data.topics.get(0));
        }
        if(data.hasRepost){
            mLine.setVisibility(VISIBLE);
            mRepostName.setVisibility(VISIBLE);
            mRepostText.setVisibility(VISIBLE);
            mRepostName.setText(data.repostName);
            mRepostText.setText(data.repostText);
        }else{
            mLine.setVisibility(GONE);
            mRepostName.setVisibility(GONE);
            mRepostText.setVisibility(GONE);
        }
        if(data.commentCount != 0){
            mCommentCount.setText(data.commentCount + "");
        }else{
            mCommentCount.setText("评论");
        }
        if(data.repostCount != 0){
            mRepostCount.setText(data.repostCount + "");
        }else{
            mRepostCount.setText("转发");
        }

        mText.setText(data.text);
        mName.setText(data.name);
        mTime.setText(data.time);
        ImageLoader.getInstance().displayImage(data.headUrl, mHead, mOptions);
    }

    private static final DisplayImageOptions mOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(600))
            .build();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.DATA, mData);
        intent.putExtra(DetailActivity.VIEW_TYPE, DetailActivity.VIEW_TYPE_PIC);
        intent.putExtra(DetailActivity.VIEW_PICS_ITEM_ID, position);
        getContext().startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.timeline_listitem_pic:
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.DATA, mData);
                intent.putExtra(DetailActivity.VIEW_TYPE, DetailActivity.VIEW_TYPE_PIC);
                intent.putExtra(DetailActivity.VIEW_PICS_ITEM_ID, 0);
                getContext().startActivity(intent);
                break;
            case R.id.timeline_listitem_head:

                break;
        }
    }
}