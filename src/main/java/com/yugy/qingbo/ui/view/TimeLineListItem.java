package com.yugy.qingbo.ui.view;

import android.content.Context;
import android.content.Intent;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yugy.qingbo.R;
import com.yugy.qingbo.Utils.NetworkUtils;
import com.yugy.qingbo.Utils.ScreenUtils;
import com.yugy.qingbo.Utils.TextUtils;
import com.yugy.qingbo.ui.activity.DetailActivity;
import com.yugy.qingbo.model.TimeLineModel;

import java.util.ArrayList;

/**
 * Created by yugy on 13-10-4.
 */
public class TimeLineListItem extends RelativeLayout implements View.OnClickListener, AdapterView.OnItemClickListener{

    public TimeLineListItem(Context context, int type) {
        super(context);
        init(type);
    }

    private HeadIconImageView head;
    private TextView name;
    private TextView text;
    private TextView topic;
    private TextView time;
    private NoScrollGridView gridView;
    private SelectorImageView pic;
    private TextView repostName;
    private TextView repostText;
    private TextView commentCount;
    private TextView repostCount;

    private TimeLineModel data;

    private void init(int type){
        switch (type){
            case TimeLineModel.TYPE_NORMAL:
                inflate(getContext(), R.layout.widget_timeline_listitem, this);
                initCommonViews();
                break;
            case TimeLineModel.TYPE_ONE_PIC:
                inflate(getContext(), R.layout.widget_timeline_listitem_one_pic, this);
                initCommonViews();
                initOnePicViews();
                break;
            case TimeLineModel.TYPE_MULTI_PIC:
                inflate(getContext(), R.layout.widget_timeline_listitem_multi_pic, this);
                initCommonViews();
                initMultiPicViews();
                break;
            case TimeLineModel.TYPE_REPOST:
                inflate(getContext(), R.layout.widget_timeline_listitem_repost, this);
                initCommonViews();
                initRepostViews();
                break;
            case TimeLineModel.TYPE_REPOST_ONE_PIC:
                inflate(getContext(), R.layout.widget_timeline_listitem_repost_one_pic, this);
                initCommonViews();
                initOnePicViews();
                initRepostViews();
                break;
            case TimeLineModel.TYPE_REPOST_MULTI_PIC:
                inflate(getContext(), R.layout.widget_timeline_listitem_multi_pic, this);
                initCommonViews();
                initMultiPicViews();
                initRepostViews();
                break;
        }
    }

    private void initCommonViews(){
        head = (HeadIconImageView) findViewById(R.id.timeline_listitem_head);
        name = (TextView) findViewById(R.id.timeline_listitem_name);
        text = (TextView) findViewById(R.id.timeline_listitem_text);
        text.setMovementMethod(LinkMovementMethod.getInstance());
        topic = (TextView) findViewById(R.id.timeline_listitem_topic);
        time = (TextView) findViewById(R.id.timeline_listitem_time);
        commentCount = (TextView) findViewById(R.id.timeline_listitem_commentcount);
        repostCount = (TextView) findViewById(R.id.timeline_listitem_repostcount);
        commentCount.setOnClickListener(this);
        repostCount.setOnClickListener(this);
    }

    private void initOnePicViews(){
        pic = (SelectorImageView) findViewById(R.id.timeline_listitem_pic);
        pic.setOnClickListener(this);
    }

    private void initMultiPicViews(){
        gridView = (NoScrollGridView) findViewById(R.id.timeline_listitem_picgrid);
        gridView.setSelector(getResources().getDrawable(R.drawable.list_selector_holo));
        gridView.setOnItemClickListener(this);
    }

    private void initRepostViews(){
        repostName = (TextView) findViewById(R.id.timeline_listitem_repost_name);
        repostName.setMovementMethod(LinkMovementMethod.getInstance());
        repostText = (TextView) findViewById(R.id.timeline_listitem_repost_text);
        repostText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void parseCommonData(){
        if(data.topics.size() == 0){
            topic.setVisibility(INVISIBLE);
        }else{
            topic.setVisibility(VISIBLE);
            topic.setText(data.topics.get(0));
        }
        if(data.commentCount != 0){
            commentCount.setText(data.commentCount + "");
        }else{
            commentCount.setText("评论");
        }
        if(data.repostCount != 0){
            repostCount.setText(data.repostCount + "");
        }else{
            repostCount.setText("转发");
        }
        text.setText(data.text);
        name.setText(data.name);
        time.setText(data.time);
        ImageLoader.getInstance().displayImage(data.headUrl, head, new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.default_head)
                .showImageForEmptyUri(R.drawable.default_head)
                .showImageForEmptyUri(R.drawable.default_head)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new FadeInBitmapDisplayer(600))
                .build());
    }

    private void parseOnePicData(){
        pic.setGif(TextUtils.isGifLink(data.pics.get(0)));
        if(NetworkUtils.isWifi(getContext())){
            ImageLoader.getInstance().displayImage(data.pics.get(0).replace("thumbnail", "bmiddle"), pic);
        }else{
            ImageLoader.getInstance().displayImage(data.pics.get(0), pic);
        }
    }

    private void parseMultiPicData(){
        gridView.setAdapter(new PicsAdapter(data.pics));
    }

    private void parseRepostData(){
        repostName.setText(data.repostName);
        repostText.setText(data.repostText);
    }

    public void parse(TimeLineModel data){
        this.data = data;
        parseCommonData();
        switch (data.type){
            case TimeLineModel.TYPE_NORMAL:
                break;
            case TimeLineModel.TYPE_ONE_PIC:
                parseOnePicData();
                break;
            case TimeLineModel.TYPE_MULTI_PIC:
                parseMultiPicData();
                break;
            case TimeLineModel.TYPE_REPOST:
                parseRepostData();
                break;
            case TimeLineModel.TYPE_REPOST_ONE_PIC:
                parseOnePicData();
                parseRepostData();
                break;
            case TimeLineModel.TYPE_REPOST_MULTI_PIC:
                parseMultiPicData();
                parseRepostData();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.DATA, data);
        intent.putExtra(DetailActivity.VIEW_TYPE, DetailActivity.VIEW_TYPE_PIC);
        intent.putExtra(DetailActivity.VIEW_PICS_ITEM_ID, position);
        getContext().startActivity(intent);
    }

    private class PicsAdapter extends BaseAdapter{

        private ArrayList<String> data;
        private int imageWidth = ScreenUtils.dp(getContext(), 80);

        public PicsAdapter(ArrayList<String> model){
            data = model;
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
                image = new GifIconImageView(getContext());
                image.setLayoutParams(new AbsListView.LayoutParams(imageWidth, imageWidth));
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }
            image.setGif(TextUtils.isGifLink(data.get(position)));
            ImageLoader.getInstance().displayImage(data.get(position), image);
            return image;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.timeline_listitem_pic:
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.DATA, data);
                intent.putExtra(DetailActivity.VIEW_TYPE, DetailActivity.VIEW_TYPE_PIC);
                intent.putExtra(DetailActivity.VIEW_PICS_ITEM_ID, 0);
                getContext().startActivity(intent);
                break;
        }
    }
}
