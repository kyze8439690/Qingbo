package me.yugy.qingbo.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONObject;

import me.yugy.qingbo.R;
import me.yugy.qingbo.activity.UserActivity;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/4/21.
 */
public class DrawerFragment extends Fragment implements View.OnClickListener {

    private ImageView mCoverImage;
    private RoundedImageView mHead;
    private TextView mName;
    private TextView mLocation;

    private String mUserName;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_drawer, container, false);
        mCoverImage = (ImageView) rootView.findViewById(R.id.cover_image);
        mHead = (RoundedImageView) rootView.findViewById(R.id.head);
        mName = (TextView) rootView.findViewById(R.id.name);
        mLocation = (TextView) rootView.findViewById(R.id.location);
        rootView.findViewById(R.id.user_layout).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        final long uid = Long.decode(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("uid", "-1"));
        UserInfo userInfo = new UserInfoDataHelper(getActivity()).select(uid);
        if(userInfo != null){
            mUserName = userInfo.screenName;
            mName.setText(userInfo.screenName);
            mLocation.setText(userInfo.location);
            ImageLoader.getInstance().displayImage(userInfo.avatar,mHead, HEAD_OPTIONS);
            if(!userInfo.cover.equals("")){
                ImageLoader.getInstance().displayImage(userInfo.cover, mCoverImage, COVER_OPTIONS);
            }else{
                Weibo.getUserInfo(getActivity(), uid, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        UserInfo userInfo = new UserInfoDataHelper(getActivity()).select(uid);
                        if(userInfo != null){
                            if(!userInfo.cover.equals("")){
                                ImageLoader.getInstance().displayImage(userInfo.cover, mCoverImage, COVER_OPTIONS);
                            }
                        }
                        super.onSuccess(statusCode, headers, response);
                    }
                });
            }
        }else{
            mName.setText("Error");
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), UserActivity.class);
        intent.putExtra("userName", mUserName);
        startActivity(intent);
    }
}
