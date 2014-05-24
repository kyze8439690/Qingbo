package me.yugy.qingbo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import me.yugy.qingbo.R;
import me.yugy.qingbo.tasker.FriendShipTasker;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.utils.ScreenUtils;

/**
 * Created by yugy on 2014/4/26.
 */
public class UserHeaderViewHelper implements View.OnClickListener, FriendShipTasker.OnFriendShipListener {

    private Context mContext;
    private AbsListView.OnScrollListener mOnScrollListener;
    private CoverImageView mCover = null;
    private Button mFollowButton;
    private String mUserName;
    private boolean mIsFollowing;

    private static final DisplayImageOptions HEAD_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(200))
            .build();

    private static final DisplayImageOptions COVER_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(200))
            .build();

    public UserHeaderViewHelper(Context context){
        mContext = context;
        mOnScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(visibleItemCount == 0) return;
                if(firstVisibleItem != 0) return;

                if(mCover != null) {
                    mCover.setCurrentTranslation(view.getChildAt(0).getTop());
                }
            }
        };
    }

    public View getHeaderView(UserInfo userInfo){
        mUserName = userInfo.screenName;
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_user_header, null);
        TextView name = (TextView) view.findViewById(R.id.name);
        RoundedImageView head  = (RoundedImageView) view.findViewById(R.id.head);
        mCover = (CoverImageView) view.findViewById(R.id.cover_image);
        TextView description = (TextView) view.findViewById(R.id.description);
        TextView location = (TextView) view.findViewById(R.id.location);
        mFollowButton = (Button) view.findViewById(R.id.follow_button);
        TextView followerCount = (TextView) view.findViewById(R.id.follower_count);

        name.setText(userInfo.screenName);
        ImageLoader.getInstance().displayImage(userInfo.avatar, head, HEAD_OPTIONS);
        if(userInfo.description.equals("")){
            description.setVisibility(View.GONE);
        }else{
            description.setText(userInfo.description);
        }
        if(userInfo.location.equals("")){
            location.setVisibility(View.GONE);
        }else{
            location.setText(String.format("Lives in %s", userInfo.location));
        }
        long uid = Long.decode(PreferenceManager.getDefaultSharedPreferences(mContext).getString("uid", "-1"));
        if(uid == userInfo.uid){
            mFollowButton.setVisibility(View.GONE);
        }else{
            updateFollowState(userInfo);
            mFollowButton.setOnClickListener(this);
        }
        followerCount.setText(String.format("Has %s followers ● Following %s peoples", userInfo.followersCount, userInfo.friendsCount));

        if(!userInfo.cover.equals("")){
            ImageLoader.getInstance().displayImage(userInfo.cover, mCover, COVER_OPTIONS);
        }
        return view;
    }

    public AbsListView.OnScrollListener getOnScrollListener() {
        return mOnScrollListener;
    }

    @Override
    public void onClick(View v) {
        int action;
        if(mIsFollowing){
            action = FriendShipTasker.ACTION_DESTROY_FRIENDSHIP;
        }else{
            action = FriendShipTasker.ACTION_CREATE_FRIENDSHIP;
        }
        new FriendShipTasker(mContext, this)
                .setTarget(mUserName)
                .setAction(action)
                .execute();
    }

    @Override
    public void onSuccess(UserInfo userInfo) {
        updateFollowState(userInfo);
    }

    private void updateFollowState(UserInfo userInfo){
        if(userInfo.following){
            mFollowButton.setBackgroundResource(R.drawable.btn_30_blue);
            int vPadding = ScreenUtils.dp(mContext, 37.5f);
            int hPadding = ScreenUtils.dp(mContext, 16);
            mFollowButton.setPadding(vPadding, hPadding, vPadding, hPadding);
            mFollowButton.setText("Following");
        }else{
            mFollowButton.setBackgroundResource(R.drawable.btn_30_red);
            int vPadding = ScreenUtils.dp(mContext, 37.5f);
            int hPadding = ScreenUtils.dp(mContext, 16);
            mFollowButton.setPadding(vPadding, hPadding, vPadding, hPadding);
            mFollowButton.setText("Follow him/her");
        }
        mIsFollowing = userInfo.following;
    }
}