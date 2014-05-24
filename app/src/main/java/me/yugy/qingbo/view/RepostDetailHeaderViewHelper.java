package me.yugy.qingbo.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import me.yugy.qingbo.R;
import me.yugy.qingbo.activity.PicActivity;
import me.yugy.qingbo.activity.UserActivity;
import me.yugy.qingbo.adapter.GridPicsAdapter;
import me.yugy.qingbo.type.RepostStatus;
import me.yugy.qingbo.utils.NetworkUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.image.HeadIconImageView;
import me.yugy.qingbo.view.image.SelectorImageView;
import me.yugy.qingbo.view.text.LinkTextView;
import me.yugy.qingbo.view.text.RelativeTimeTextView;

/**
 * Created by yugy on 2014/4/25.
 */
public class RepostDetailHeaderViewHelper implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int TYPE_NO_PIC = 0;
    private static final int TYPE_ONE_PIC = 1;
    private static final int TYPE_MULTI_PICS = 2;

    private static final DisplayImageOptions HEAD_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(200))
            .build();

    private Context mContext;
    private TextView mCommentCountView;
    private TextView mRepostCountView;

    private String[] mPicsUrl;
    private String mUsername;

    public RepostDetailHeaderViewHelper(Context context){
        mContext = context;
    }

    public TextView getCommentCountView() {
        return mCommentCountView;
    }

    public TextView getRepostCountView() {
        return mRepostCountView;
    }

    public View getHeaderView(final RepostStatus repostStatus){
        int type = getViewType(repostStatus);
        View view = LayoutInflater.from(mContext).inflate(getLayoutResourceId(type), null);
        view.setBackgroundColor(Color.WHITE);

        HeadIconImageView head = (HeadIconImageView) view.findViewById(R.id.status_listitem_head);
        TextView name = (TextView) view.findViewById(R.id.status_listitem_name);
        RelativeTimeTextView time = (RelativeTimeTextView) view.findViewById(R.id.status_listitem_time);
        TextView topics = (TextView) view.findViewById(R.id.status_listitem_topic);
        LinkTextView text = (LinkTextView) view.findViewById(R.id.status_listitem_text);
        mCommentCountView = (TextView) view.findViewById(R.id.status_listitem_comment_count);
        mRepostCountView = (TextView) view.findViewById(R.id.status_listitem_repost_count);

        //parse basic data
        ImageLoader.getInstance().displayImage(repostStatus.user.avatar, head, HEAD_OPTIONS);
        head.setOnClickListener(this);
        mUsername = repostStatus.user.screenName;
        name.setText(repostStatus.user.screenName);
        time.setReferenceTime(repostStatus.time);
        if(repostStatus.topics.length != 0){
            topics.setVisibility(View.VISIBLE);
            topics.setText(repostStatus.topics[0]);
        }else{
            topics.setVisibility(View.INVISIBLE);
        }
        text.setText(repostStatus.text);
        if(repostStatus.commentCount == 0){
            mCommentCountView.setText("");
        }else {
            mCommentCountView.setText(String.valueOf(repostStatus.commentCount));
        }
        if(repostStatus.repostCount == 0){
            mRepostCountView.setText("");
        }else {
            mRepostCountView.setText(String.valueOf(repostStatus.repostCount));
        }
        if(type == TYPE_ONE_PIC){
            SelectorImageView pic = (SelectorImageView) view.findViewById(R.id.status_listitem_pic);
            pic.setGif(TextUtils.isGifLink(repostStatus.pics[0]));
            if(!NetworkUtils.isWifi() || TextUtils.isGifLink(repostStatus.pics[0])){
                ImageLoader.getInstance().displayImage(repostStatus.pics[0], pic);
            }else {
                ImageLoader.getInstance().displayImage(repostStatus.pics[0].replace("thumbnail", "bmiddle"), pic);
            }
            mPicsUrl = repostStatus.pics;
            pic.setOnClickListener(this);
        }else if(type == TYPE_MULTI_PICS){
            NoScrollGridView pics = (NoScrollGridView) view.findViewById(R.id.status_listitem_picgrid);
            pics.setAdapter(new GridPicsAdapter(mContext, repostStatus.pics, NetworkUtils.isWifi()));
            mPicsUrl = repostStatus.pics;
            pics.setOnItemClickListener(this);
        }

        return view;
    }

    private int getViewType(RepostStatus repostStatus){
        if(!repostStatus.hasPic && !repostStatus.hasPics){
            return TYPE_NO_PIC;
        }else if(repostStatus.hasPic && !repostStatus.hasPics){
            return TYPE_ONE_PIC;
        }else if(repostStatus.hasPics && !repostStatus.hasPic){
            return TYPE_MULTI_PICS;
        }else{
            return -1;
        }
    }

    private int getLayoutResourceId(int type){
        switch (type){
            case TYPE_NO_PIC: return R.layout.view_status_item_no_repost_no_pic;
            case TYPE_ONE_PIC: return R.layout.view_status_item_no_repost_one_pic;
            case TYPE_MULTI_PICS: return R.layout.view_status_item_no_repost_multi_pics;
            default: return 0;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.status_listitem_head:
                Intent intent = new Intent(mContext, UserActivity.class);
                intent.putExtra("userName", mUsername);
                mContext.startActivity(intent);
                break;
            case R.id.status_listitem_comment_count:

                break;
            case R.id.status_listitem_repost_count:

                break;
            case R.id.status_listitem_pic:
                intent = new Intent(mContext, PicActivity.class);
                intent.putExtra("pics", mPicsUrl);
                mContext.startActivity(intent);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, PicActivity.class);
        intent.putExtra("pics", mPicsUrl);
        intent.putExtra("position", position);
        mContext.startActivity(intent);
    }
}
