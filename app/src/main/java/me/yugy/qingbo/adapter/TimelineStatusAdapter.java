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
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
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

    private OnLoadMoreListener mOnLoadMoreListener;
    private Context mContext;
    private boolean mIsWifi = NetworkUtils.isWifi();
    private LayoutInflater mLayoutInflater;

    public TimelineStatusAdapter(Context context) {
        super(context, null, false);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        try{
            mOnLoadMoreListener = (OnLoadMoreListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException("Activity should implement the OnLoadMoreListener interface.");
        }
    }

    @Override
    protected void onContentChanged() {
        mIsWifi = NetworkUtils.isWifi();
        super.onContentChanged();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        NoRepostNoPicViewHolder viewHolder;
        int type = getItemViewType(cursor);
        switch (type){
            case Status.TYPE_NO_REPOST_NO_PIC:
                view = mLayoutInflater.inflate(R.layout.view_status_item_no_repost_no_pic, null);
                viewHolder = new NoRepostNoPicViewHolder(view);
                break;
            case Status.TYPE_NO_REPOST_ONE_PIC:
                view = mLayoutInflater.inflate(R.layout.view_status_item_no_repost_one_pic, null);
                viewHolder = new NoRepostOnePicViewHolder(view);
                break;
            case Status.TYPE_NO_REPOST_MULTI_PICS:
                view = mLayoutInflater.inflate(R.layout.view_status_item_no_repost_multi_pics, null);
                viewHolder = new NoRepostMultiPicsViewHolder(view);
                break;
            case Status.TYPE_HAS_REPOST_NO_PIC:
                view = mLayoutInflater.inflate(R.layout.view_status_item_has_repost_no_pic, null);
                viewHolder = new HasRepostNoPicViewHolder(view);
                break;
            case Status.TYPE_HAS_REPOST_ONE_PIC:
                view = mLayoutInflater.inflate(R.layout.view_status_item_has_repost_one_pic, null);
                viewHolder = new HasRepostOnePicViewHolder(view);
                break;
            case Status.TYPE_HAS_REPOST_MULTI_PICS:
                view = mLayoutInflater.inflate(R.layout.view_status_item_has_repost_multi_pics, null);
                viewHolder = new HasRepostMultiPicsViewHolder(view);
                break;
            default:
                view = null;
                viewHolder = null;
        }
        view.setTag(viewHolder);
        view.setTag(R.id.timeline_holder_type, type);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

//        Debug.startMethodTracing("qingbo");

        int position = cursor.getPosition();

        try {
            Status status = Status.fromCursor(cursor);
            int type = (Integer) view.getTag(R.id.timeline_holder_type);
            switch (type){
                case Status.TYPE_NO_REPOST_NO_PIC:
                    ((NoRepostNoPicViewHolder) view.getTag()).parse(status);
                    break;
                case Status.TYPE_NO_REPOST_ONE_PIC:
                    ((NoRepostOnePicViewHolder) view.getTag()).parse(status);
                    break;
                case Status.TYPE_NO_REPOST_MULTI_PICS:
                    ((NoRepostMultiPicsViewHolder) view.getTag()).parse(status);
                    break;
                case Status.TYPE_HAS_REPOST_NO_PIC:
                    ((HasRepostNoPicViewHolder) view.getTag()).parse(status);
                    break;
                case Status.TYPE_HAS_REPOST_ONE_PIC:
                    ((HasRepostOnePicViewHolder) view.getTag()).parse(status);
                    break;
                case Status.TYPE_HAS_REPOST_MULTI_PICS:
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

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    public int getItemViewType(Cursor cursor){
        int type = cursor.getInt(cursor.getColumnIndex(StatusDBInfo.TYPE));
        return type;
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
            if(!mIsWifi || TextUtils.isGifLink(status.pics[0])){
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
            if(!mIsWifi || TextUtils.isGifLink(status.repostStatus.pics[0])) {
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
            pics.setAdapter(new GridPicsAdapter(mContext, status.pics, mIsWifi));
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
            pics.setAdapter(new GridPicsAdapter(mContext, status.repostStatus.pics, mIsWifi));
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