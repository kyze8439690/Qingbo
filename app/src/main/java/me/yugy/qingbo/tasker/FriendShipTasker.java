package me.yugy.qingbo.tasker;

import android.app.ProgressDialog;
import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import me.yugy.qingbo.R;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/4/30.
 */
public class FriendShipTasker {

    public static final int ACTION_CREATE_FRIENDSHIP = 0;
    public static final int ACTION_DESTROY_FRIENDSHIP = 1;

    private Context mContext;
    private ProgressDialog mProgressDialog;
    private OnFriendShipListener mOnFriendShipListener;

    private String mUserName;
    private int mAction;

    public FriendShipTasker(Context context, OnFriendShipListener onFriendShipListener){
        mContext = context;
        mOnFriendShipListener = onFriendShipListener;
        mProgressDialog = new ProgressDialog(context, ProgressDialog.THEME_HOLO_LIGHT);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setIndeterminate(true);
    }

    public FriendShipTasker setTarget(String userName){
        mUserName = userName;
        return this;
    }

    public FriendShipTasker setAction(int action){
        mAction = action;
        if(mAction == ACTION_CREATE_FRIENDSHIP){
            mProgressDialog.setMessage(mContext.getString(R.string.create_friendship));
        }else if(mAction == ACTION_DESTROY_FRIENDSHIP) {
            mProgressDialog.setMessage(mContext.getString(R.string.destroy_friendship));
        }
        return this;
    }

    public void execute(){
        mProgressDialog.show();
        JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    UserInfo userInfo = new UserInfo();
                    userInfo.parse(response);
                    if(mAction == ACTION_CREATE_FRIENDSHIP){
                        userInfo.following = true;
                    }else if(mAction == ACTION_DESTROY_FRIENDSHIP){
                        userInfo.following = false;
                    }
                    new UserInfoDataHelper(mContext).insert(userInfo);
                    mProgressDialog.dismiss();
                    mOnFriendShipListener.onSuccess(userInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }
        };
        if(mAction == ACTION_CREATE_FRIENDSHIP){
            Weibo.createFriendShip(mContext, mUserName, handler);
        }else if(mAction == ACTION_DESTROY_FRIENDSHIP) {
            Weibo.destroyFriendShip(mContext, mUserName, handler);
        }
    }

    public static interface OnFriendShipListener{
        public void onSuccess(UserInfo userInfo);
    }
}
