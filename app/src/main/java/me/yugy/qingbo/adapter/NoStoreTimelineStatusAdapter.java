package me.yugy.qingbo.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.ArrayList;
import java.util.List;

import me.yugy.qingbo.R;
import me.yugy.qingbo.activity.PicActivity;
import me.yugy.qingbo.activity.UserActivity;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.NetworkUtils;
import me.yugy.qingbo.utils.ScreenUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.NoScrollGridView;
import me.yugy.qingbo.view.image.HeadIconImageView;
import me.yugy.qingbo.view.image.SelectorImageView;
import me.yugy.qingbo.view.text.LinkTextView;
import me.yugy.qingbo.view.text.RelativeTimeTextView;

/**
 * Created by yugy on 2014/4/30.
 */
public class NoStoreTimelineStatusAdapter extends BaseAdapter{

    private static final DisplayImageOptions HEAD_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(200))
            .build();

    private ArrayList<Status> mStatuses;
    private OnLoadMoreListener mOnLoadMoreListener;
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private boolean mIsWifi = NetworkUtils.isWifi();

    public NoStoreTimelineStatusAdapter(Context context, ArrayList<Status> statuses){
        mContext = context;
        try {
            mOnLoadMoreListener = (OnLoadMoreListener) context;
        }catch (ClassCastException e){
            throw new ClassCastException("The activity must implement the OnLoadMoreListener");
        }
        mLayoutInflater = LayoutInflater.from(context);
        mStatuses = statuses;
    }

    @Override
    public int getCount() {
        return mStatuses.size();
    }

    @Override
    public Status getItem(int position) {
        return mStatuses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        NoRepostNoPicViewHolder viewHolder;
        int type = getItemViewType(position);
        Status status = getItem(position);
        if(view == null) {
            view = new FrameLayout(mContext);
            view.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            int vPadding = ScreenUtils.dp(mContext, 8);
            view.setPadding(vPadding, 0, vPadding, 0);
            ((FrameLayout)view).setForeground(mContext.getResources().getDrawable(R.drawable.user_listitem_selector));
            switch (type) {
                case Status.TYPE_NO_REPOST_NO_PIC:
                    mLayoutInflater.inflate(R.layout.view_status_detail_no_repost_no_pic, (ViewGroup) view);
                    viewHolder = new NoRepostNoPicViewHolder(view);
                    break;
                case Status.TYPE_NO_REPOST_ONE_PIC:
                    mLayoutInflater.inflate(R.layout.view_status_detail_no_repost_one_pic, (ViewGroup) view);
                    viewHolder = new NoRepostOnePicViewHolder(view);
                    break;
                case Status.TYPE_NO_REPOST_MULTI_PICS:
                    mLayoutInflater.inflate(R.layout.view_status_detail_no_repost_multi_pics, (ViewGroup) view);
                    viewHolder = new NoRepostMultiPicsViewHolder(view);
                    break;
                case Status.TYPE_HAS_REPOST_NO_PIC:
                    mLayoutInflater.inflate(R.layout.view_status_detail_has_repost_no_pic, (ViewGroup) view);
                    viewHolder = new HasRepostNoPicViewHolder(view);
                    break;
                case Status.TYPE_HAS_REPOST_ONE_PIC:
                    mLayoutInflater.inflate(R.layout.view_status_detail_has_repost_one_pic, (ViewGroup) view);
                    viewHolder = new HasRepostOnePicViewHolder(view);
                    break;
                case Status.TYPE_HAS_REPOST_MULTI_PICS:
                    mLayoutInflater.inflate(R.layout.view_status_detail_has_repost_multi_pics, (ViewGroup) view);
                    viewHolder = new HasRepostMultiPicsViewHolder(view);
                    break;
                default:
                    viewHolder = null;
            }
            view.setTag(viewHolder);
        }
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
        if(position == getCount() - 1){
            mOnLoadMoreListener.onLoadMore();
        }
        return view;
    }

    @Override
    public int getViewTypeCount() {
        return 6;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public void notifyDataSetChanged() {
        mIsWifi = NetworkUtils.isWifi();
        super.notifyDataSetChanged();
    }

    public void addNewStatuses(List<Status> statuses){
        mStatuses.addAll(0, statuses);
        notifyDataSetChanged();
    }

    public void addOldStatuses(List<Status> statuses){
        mStatuses.addAll(mStatuses.size(), statuses);
        notifyDataSetChanged();
    }

    private class NoRepostNoPicViewHolder implements View.OnClickListener {

        public HeadIconImageView head;
        public TextView name;
        public RelativeTimeTextView time;
        public LinkTextView text;
        public TextView commentCount;
        public TextView repostCount;

        private String mUserName;

        public NoRepostNoPicViewHolder(View view){
            head = (HeadIconImageView) view.findViewById(R.id.head);
            name = (TextView) view.findViewById(R.id.status_listitem_name);
            time = (RelativeTimeTextView) view.findViewById(R.id.status_listitem_time);
            text = (LinkTextView) view.findViewById(R.id.status_listitem_text);
            commentCount = (TextView) view.findViewById(R.id.status_listitem_comment_count);
            repostCount = (TextView) view.findViewById(R.id.status_listitem_repost_count);
        }

        public void parse(Status status){
            mUserName = status.user.screenName;
            ImageLoader.getInstance().displayImage(status.user.avatar, head, HEAD_OPTIONS);
            name.setText(status.user.screenName);
            time.setReferenceTime(status.time);
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
            if(v.getId() == R.id.head){
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
