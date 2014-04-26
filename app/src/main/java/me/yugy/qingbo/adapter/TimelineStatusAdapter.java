package me.yugy.qingbo.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.json.JSONException;

import java.text.ParseException;

import me.yugy.qingbo.R;
import me.yugy.qingbo.activity.PicActivity;
import me.yugy.qingbo.activity.UserActivity;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.NetworkUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.NoScrollGridView;
import me.yugy.qingbo.view.image.HeadIconImageView;
import me.yugy.qingbo.view.image.SelectorImageView;
import me.yugy.qingbo.view.text.LinkTextView;
import me.yugy.qingbo.view.text.RelativeTimeTextView;

import static android.view.View.OnClickListener;

/**
 * Created by yugy on 2014/4/16.
 */
public class TimelineStatusAdapter extends CursorAdapter{

    private static final int TYPE_NO_REPOST_NO_PIC = 0;
    private static final int TYPE_NO_REPOST_ONE_PIC = 1;
    private static final int TYPE_NO_REPOST_MULTI_PICS = 2;
    private static final int TYPE_HAS_REPOST_NO_PIC = 3;
    private static final int TYPE_HAS_REPOST_ONE_PIC = 4;
    private static final int TYPE_HAS_REPOST_MULTI_PICS = 5;

    private OnLoadMoreListener mOnLoadMoreListener;
    private Context mContext;

    public TimelineStatusAdapter(Context context) {
        super(context, null, false);
        mContext = context;
        try{
            mOnLoadMoreListener = (OnLoadMoreListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException("Activity should implement the OnLoadMoreListener interface.");
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        NoRepostNoPicViewHolder viewHolder;
        Status status = null;
        try {
            status = Status.fromCursor(cursor);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        switch (getItemViewType(status)){
            case TYPE_NO_REPOST_NO_PIC:
                view = LayoutInflater.from(context).inflate(R.layout.view_status_item_no_repost_no_pic, null);
                viewHolder = new NoRepostNoPicViewHolder(view);
                break;
            case TYPE_NO_REPOST_ONE_PIC:
                view = LayoutInflater.from(context).inflate(R.layout.view_status_item_no_repost_one_pic, null);
                viewHolder = new NoRepostOnePicViewHolder(view);
                break;
            case TYPE_NO_REPOST_MULTI_PICS:
                view = LayoutInflater.from(context).inflate(R.layout.view_status_item_no_repost_multi_pics, null);
                viewHolder = new NoRepostMultiPicsViewHolder(view);
                break;
            case TYPE_HAS_REPOST_NO_PIC:
                view = LayoutInflater.from(context).inflate(R.layout.view_status_item_has_repost_no_pic, null);
                viewHolder = new HasRepostNoPicViewHolder(view);
                break;
            case TYPE_HAS_REPOST_ONE_PIC:
                view = LayoutInflater.from(context).inflate(R.layout.view_status_item_has_repost_one_pic, null);
                viewHolder = new HasRepostOnePicViewHolder(view);
                break;
            case TYPE_HAS_REPOST_MULTI_PICS:
                view = LayoutInflater.from(context).inflate(R.layout.view_status_item_has_repost_multi_pics, null);
                viewHolder = new HasRepostMultiPicsViewHolder(view);
                break;
            default:
                view = null;
                viewHolder = null;
        }
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

//        Debug.startMethodTracing("qingbo");

        int position = cursor.getPosition();

        try {
            Status status = Status.fromCursor(cursor);
            switch (getItemViewType(status)){
                case TYPE_NO_REPOST_NO_PIC:
                    ((NoRepostNoPicViewHolder) view.getTag()).parse(status);
                    break;
                case TYPE_NO_REPOST_ONE_PIC:
                    ((NoRepostOnePicViewHolder) view.getTag()).parse(status);
                    break;
                case TYPE_NO_REPOST_MULTI_PICS:
                    ((NoRepostMultiPicsViewHolder) view.getTag()).parse(status);
                    break;
                case TYPE_HAS_REPOST_NO_PIC:
                    ((HasRepostNoPicViewHolder) view.getTag()).parse(status);
                    break;
                case TYPE_HAS_REPOST_ONE_PIC:
                    ((HasRepostOnePicViewHolder) view.getTag()).parse(status);
                    break;
                case TYPE_HAS_REPOST_MULTI_PICS:
                    ((HasRepostMultiPicsViewHolder) view.getTag()).parse(status);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(position == getCount() - 1){
            DebugUtils.log("load more");
            mOnLoadMoreListener.onLoadMore();
        }

//        Debug.stopMethodTracing();
    }

    /**
     * 0 : no repost, no pic
     * 1 : no repost, one pic
     * 2 : no repost, multi pics
     * 3 : has repost, no pic
     * 4 : has repost, one pic
     * 5 : has repost, multi pics
     * @return
     */
    @Override
    public int getViewTypeCount() {
        return 6;
    }

    public int getItemViewType(Status status){
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

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        Status status = null;
        try {
            status = Status.fromCursor(cursor);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return getItemViewType(status);
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

    private class NoRepostNoPicViewHolder implements OnClickListener{

        public HeadIconImageView head;
        public TextView name;
        public RelativeTimeTextView time;
        public TextView topics;
        public LinkTextView text;
        public TextView commentCount;
        public TextView repostCount;

        private String mUserName;

        public NoRepostNoPicViewHolder(View view){
            head = (HeadIconImageView) view.findViewById(R.id.status_listitem_head);
            name = (TextView) view.findViewById(R.id.status_listitem_name);
            time = (RelativeTimeTextView) view.findViewById(R.id.status_listitem_time);
            topics = (TextView) view.findViewById(R.id.status_listitem_topic);
            text = (LinkTextView) view.findViewById(R.id.status_listitem_text);
            commentCount = (TextView) view.findViewById(R.id.status_listitem_comment_count);
            repostCount = (TextView) view.findViewById(R.id.status_listitem_repost_count);
        }

        public void parse(Status status){
            mUserName = status.user.screenName;
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
                commentCount.setText("");
            }else {
                commentCount.setText(String.valueOf(status.commentCount));
            }
            if(status.repostCount == 0){
                repostCount.setText("");
            }else {
                repostCount.setText(String.valueOf(status.repostCount));
            }
            head.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.status_listitem_head){
                Intent intent = new Intent(mContext, UserActivity.class);
                intent.putExtra("userName", mUserName);
                mContext.startActivity(intent);
            }
        }
    }

    private class HasRepostNoPicViewHolder extends NoRepostNoPicViewHolder{

        public TextView repostName;
        public LinkTextView repostText;

        public HasRepostNoPicViewHolder(View view) {
            super(view);
            repostName = (TextView) view.findViewById(R.id.status_listitem_repost_name);
            repostText = (LinkTextView) view.findViewById(R.id.status_listitem_repost_text);
        }

        @Override
        public void parse(Status status) {
            super.parse(status);
            repostName.setText(String.format("此微博最初是由@%s 分享的", status.repostStatus.user.screenName));
            repostText.setText(status.repostStatus.text);
        }
    }

    private class NoRepostOnePicViewHolder extends NoRepostNoPicViewHolder{

        public SelectorImageView pic;
        public String[] picsUrl;

        public NoRepostOnePicViewHolder(View view) {
            super(view);
            pic = (SelectorImageView) view.findViewById(R.id.status_listitem_pic);
        }

        @Override
        public void parse(Status status) {
            super.parse(status);
            pic.setGif(TextUtils.isGifLink(status.pics[0]));
            if(!NetworkUtils.isWifi() || TextUtils.isGifLink(status.pics[0])){
                ImageLoader.getInstance().displayImage(status.pics[0], pic);
            }else {
                ImageLoader.getInstance().displayImage(status.pics[0].replace("thumbnail", "bmiddle"), pic);
            }
            picsUrl = status.pics;
            pic.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            super.onClick(v);
            if(v.getId() == R.id.status_listitem_pic){
                Intent intent = new Intent(mContext, PicActivity.class);
                intent.putExtra("pics", picsUrl);
                mContext.startActivity(intent);
            }
        }
    }

    private class HasRepostOnePicViewHolder extends NoRepostNoPicViewHolder{

        public TextView repostName;
        public LinkTextView repostText;
        public SelectorImageView pic;
        public String[] picsUrl;

        public HasRepostOnePicViewHolder(View view) {
            super(view);
            repostName = (TextView) view.findViewById(R.id.status_listitem_repost_name);
            repostText = (LinkTextView) view.findViewById(R.id.status_listitem_repost_text);
            pic = (SelectorImageView) view.findViewById(R.id.status_listitem_pic);
        }

        @Override
        public void parse(Status status) {
            super.parse(status);
            pic.setGif(TextUtils.isGifLink(status.repostStatus.pics[0]));
            if(!NetworkUtils.isWifi() || TextUtils.isGifLink(status.repostStatus.pics[0])) {
                ImageLoader.getInstance().displayImage(status.repostStatus.pics[0], pic);
            }else{
                ImageLoader.getInstance().displayImage(status.repostStatus.pics[0].replace("thumbnail", "bmiddle"), pic);
            }
            picsUrl = status.repostStatus.pics;
            pic.setOnClickListener(this);
            repostName.setText(String.format("此微博最初是由@%s 分享的", status.repostStatus.user.screenName));
            repostText.setText(status.repostStatus.text);
        }

        @Override
        public void onClick(View v) {
            super.onClick(v);
            if(v.getId() == R.id.status_listitem_pic){
                Intent intent = new Intent(mContext, PicActivity.class);
                intent.putExtra("pics", picsUrl);
                mContext.startActivity(intent);
            }
        }
    }

    private class NoRepostMultiPicsViewHolder extends NoRepostNoPicViewHolder implements AdapterView.OnItemClickListener{

        public NoScrollGridView pics;
        private String[] picsUrl;

        public NoRepostMultiPicsViewHolder(View view) {
            super(view);
            pics = (NoScrollGridView) view.findViewById(R.id.status_listitem_picgrid);
        }

        @Override
        public void parse(Status status) {
            super.parse(status);
            pics.setAdapter(new GridPicsAdapter(mContext, status.pics));
            picsUrl = status.pics;
            pics.setOnItemClickListener(this);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(view.getContext(), PicActivity.class);
            intent.putExtra("pics", picsUrl);
            intent.putExtra("position", position);
            view.getContext().startActivity(intent);
        }
    }

    private class HasRepostMultiPicsViewHolder extends NoRepostNoPicViewHolder implements AdapterView.OnItemClickListener{

        public TextView repostName;
        public LinkTextView repostText;
        public NoScrollGridView pics;
        private String[] picsUrl;

        public HasRepostMultiPicsViewHolder(View view) {
            super(view);
            repostName = (TextView) view.findViewById(R.id.status_listitem_repost_name);
            repostText = (LinkTextView) view.findViewById(R.id.status_listitem_repost_text);
            pics = (NoScrollGridView) view.findViewById(R.id.status_listitem_picgrid);
        }

        @Override
        public void parse(Status status) {
            super.parse(status);
            repostName.setText(String.format("此微博最初是由@%s 分享的", status.repostStatus.user.screenName));
            repostText.setText(status.repostStatus.text);
            pics.setAdapter(new GridPicsAdapter(mContext, status.repostStatus.pics));
            picsUrl = status.repostStatus.pics;
            pics.setOnItemClickListener(this);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(view.getContext(), PicActivity.class);
            intent.putExtra("pics", picsUrl);
            intent.putExtra("position", position);
            view.getContext().startActivity(intent);
        }
    }
}