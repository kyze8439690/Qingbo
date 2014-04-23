package me.yugy.qingbo.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import me.yugy.qingbo.R;
import me.yugy.qingbo.activity.PicActivity;
import me.yugy.qingbo.adapter.GridPicsAdapter;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.utils.NetworkUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.image.HeadIconImageView;
import me.yugy.qingbo.view.image.SelectorImageView;
import me.yugy.qingbo.view.text.LinkTextView;
import me.yugy.qingbo.view.text.RelativeTimeTextView;
import me.yugy.qingbo.view.text.TouchClickableSpan;

/**
 * Created by yugy on 2014/4/20.
 */
public class DetailHeaderViewHelper implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final int TYPE_NO_REPOST_NO_PIC = 0;
    private static final int TYPE_NO_REPOST_ONE_PIC = 1;
    private static final int TYPE_NO_REPOST_MULTI_PICS = 2;
    private static final int TYPE_HAS_REPOST_NO_PIC = 3;
    private static final int TYPE_HAS_REPOST_ONE_PIC = 4;
    private static final int TYPE_HAS_REPOST_MULTI_PICS = 5;

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

    private String[] picsUrl;

    public DetailHeaderViewHelper(Context context){
        mContext = context;
    }

    public TextView getCommentCountView() {
        return mCommentCountView;
    }

    public TextView getRepostCountView() {
        return mRepostCountView;
    }

    public View getHeaderView(final Status status){
        int type = getViewType(status);
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
        ImageLoader.getInstance().displayImage(status.user.avatar, head, HEAD_OPTIONS);
        name.setText(status.user.screenName);
        time.setReferenceTime(status.time);
        if(status.topics.length != 0){
            topics.setVisibility(View.VISIBLE);
            topics.setText(status.topics[0]);
        }else{
            topics.setVisibility(View.INVISIBLE);
        }
        text.setText(status.text);
        if(status.commentCount == 0){
            mCommentCountView.setText("");
        }else {
            mCommentCountView.setText(String.valueOf(status.commentCount));
        }
        if(status.repostCount == 0){
            mRepostCountView.setText("");
        }else {
            mRepostCountView.setText(String.valueOf(status.repostCount));
        }

        //parse different data
        if(type == TYPE_HAS_REPOST_NO_PIC){
            LinkTextView repostName = (LinkTextView) view.findViewById(R.id.status_listitem_repost_name);
            LinkTextView repostText = (LinkTextView) view.findViewById(R.id.status_listitem_repost_text);
            SpannableString repostNameString = TextUtils.getClickForWholeText(
                    String.format("此微博最初是由@%s 分享的", status.repostStatus.user.screenName),
                    new TouchClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            MessageUtils.toast(widget.getContext(), status.repostStatus.user.screenName);
                        }
                    });
            repostName.setText(repostNameString);
            repostText.setText(status.repostStatus.text);
        }else if(type == TYPE_NO_REPOST_ONE_PIC){
            SelectorImageView pic = (SelectorImageView) view.findViewById(R.id.status_listitem_pic);
            pic.setGif(TextUtils.isGifLink(status.pics[0]));
            if(!NetworkUtils.isWifi() || TextUtils.isGifLink(status.pics[0])){
                ImageLoader.getInstance().displayImage(status.pics[0], pic);
            }else {
                ImageLoader.getInstance().displayImage(status.pics[0].replace("thumbnail", "bmiddle"), pic);
            }
            picsUrl = status.pics;
            pic.setOnClickListener(this);
        }else if(type == TYPE_HAS_REPOST_ONE_PIC){
            LinkTextView repostName = (LinkTextView) view.findViewById(R.id.status_listitem_repost_name);
            LinkTextView repostText = (LinkTextView) view.findViewById(R.id.status_listitem_repost_text);
            SelectorImageView pic = (SelectorImageView) view.findViewById(R.id.status_listitem_pic);
            pic.setGif(TextUtils.isGifLink(status.repostStatus.pics[0]));
            if(!NetworkUtils.isWifi() || TextUtils.isGifLink(status.repostStatus.pics[0])) {
                ImageLoader.getInstance().displayImage(status.repostStatus.pics[0], pic);
            }else{
                ImageLoader.getInstance().displayImage(status.repostStatus.pics[0].replace("thumbnail", "bmiddle"), pic);
            }
            picsUrl = status.repostStatus.pics;
            pic.setOnClickListener(this);
            SpannableString repostNameString = TextUtils.getClickForWholeText(
                    String.format("此微博最初是由@%s 分享的", status.repostStatus.user.screenName),
                    new TouchClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            MessageUtils.toast(widget.getContext(), status.repostStatus.user.screenName);
                        }
                    });
            repostName.setText(repostNameString);
            repostText.setText(status.repostStatus.text);
        }else if(type == TYPE_NO_REPOST_MULTI_PICS){
            NoScrollGridView pics = (NoScrollGridView) view.findViewById(R.id.status_listitem_picgrid);
            pics.setAdapter(new GridPicsAdapter(mContext, status.pics));
            picsUrl = status.pics;
            pics.setOnItemClickListener(this);
        }else if(type == TYPE_HAS_REPOST_MULTI_PICS){
            LinkTextView repostName = (LinkTextView) view.findViewById(R.id.status_listitem_repost_name);
            LinkTextView repostText = (LinkTextView) view.findViewById(R.id.status_listitem_repost_text);
            NoScrollGridView pics = (NoScrollGridView) view.findViewById(R.id.status_listitem_picgrid);
            SpannableString repostNameString = TextUtils.getClickForWholeText(
                    String.format("此微博最初是由@%s 分享的", status.repostStatus.user.screenName),
                    new TouchClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            MessageUtils.toast(widget.getContext(), status.repostStatus.user.screenName);
                        }
                    });
            repostName.setText(repostNameString);
            repostText.setText(status.repostStatus.text);
            pics.setAdapter(new GridPicsAdapter(mContext, status.repostStatus.pics));
            picsUrl = status.repostStatus.pics;
            pics.setOnItemClickListener(this);
        }

        return view;
    }

    private int getViewType(Status status){
        if(status.repostStatus == null){
            //no repost, return 0/1/2
            if(!status.hasPic && !status.hasPics){
                return TYPE_NO_REPOST_NO_PIC;
            }else if(status.hasPic && !status.hasPics){
                return TYPE_NO_REPOST_ONE_PIC;
            }else if(status.hasPics && !status.hasPic){
                return TYPE_NO_REPOST_MULTI_PICS;
            }else{
                return -1;
            }
        }else{
            //has repost, return 3/4/5
            if(!status.repostStatus.hasPic && !status.repostStatus.hasPics){
                return TYPE_HAS_REPOST_NO_PIC;
            }else if(status.repostStatus.hasPic && !status.repostStatus.hasPics){
                return TYPE_HAS_REPOST_ONE_PIC;
            }else if(status.repostStatus.hasPics && !status.repostStatus.hasPic){
                return TYPE_HAS_REPOST_MULTI_PICS;
            }else{
                return -1;
            }
        }
    }

    private int getLayoutResourceId(int type){
        switch (type){
            case TYPE_NO_REPOST_NO_PIC: return R.layout.view_status_item_no_repost_no_pic;
            case TYPE_NO_REPOST_ONE_PIC: return R.layout.view_status_item_no_repost_one_pic;
            case TYPE_NO_REPOST_MULTI_PICS: return R.layout.view_status_item_no_repost_multi_pics;
            case TYPE_HAS_REPOST_NO_PIC: return R.layout.view_status_item_has_repost_no_pic;
            case TYPE_HAS_REPOST_ONE_PIC: return R.layout.view_status_item_has_repost_one_pic;
            case TYPE_HAS_REPOST_MULTI_PICS: return R.layout.view_status_item_has_repost_multi_pics;
            default: return 0;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.status_listitem_comment_count:

                break;
            case R.id.status_listitem_repost_count:

                break;
            case R.id.status_listitem_pic:
                Intent intent = new Intent(mContext, PicActivity.class);
                intent.putExtra("pics", picsUrl);
                mContext.startActivity(intent);
                break;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(mContext, PicActivity.class);
        intent.putExtra("pics", picsUrl);
        intent.putExtra("position", position);
        mContext.startActivity(intent);
    }
}
