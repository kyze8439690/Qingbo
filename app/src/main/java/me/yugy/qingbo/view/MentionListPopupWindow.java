package me.yugy.qingbo.view;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListPopupWindow;
import android.widget.ProgressBar;

import com.google.analytics.containertag.common.Key;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import me.yugy.qingbo.adapter.MentionAdapter;
import me.yugy.qingbo.dao.datahelper.UserIndexDataHelper;
import me.yugy.qingbo.dao.dbinfo.UserIndexDBInfo;
import me.yugy.qingbo.type.UserIndex;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/5/30.
 */
public class MentionListPopupWindow extends ListPopupWindow implements AdapterView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    public static final int TYPE_LOADING = 0;
    public static final int TYPE_NORMAL = 1;

    private int mType;

    private String mKeyword;

    private Context mContext;
    private UserIndexDataHelper mUserIndexDataHelper;
    private MentionAdapter mMentionAdapter;
    private OnMentionSelectListener mOnMentionSelectListener;

    public MentionListPopupWindow(Context context, String keyword) {
        super(context);
        mContext = context;
        mKeyword = keyword;
        mUserIndexDataHelper = new UserIndexDataHelper(context);
        mMentionAdapter = new MentionAdapter(context);
        if(mUserIndexDataHelper.getUserIndexCount() == 0){
            mType = TYPE_LOADING;
            getFriendData();
            setAdapter(new LoadingAdapter());
        }else{
            mType = TYPE_NORMAL;
            setAdapter(mMentionAdapter);
        }
        setOnItemClickListener(this);
        ((Activity) mContext).getLoaderManager().initLoader(0, null, this);
    }

    public void setOnMentionSelectListener(OnMentionSelectListener onMentionSelectListener) {
        mOnMentionSelectListener = onMentionSelectListener;
    }

    public interface OnMentionSelectListener{
        public void onMentionSelect(String name);
    }

    private void getFriendData(){
        Weibo.getFriends(mContext, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    int length = response.length();
                    ArrayList<UserInfo> userInfos = new ArrayList<UserInfo>();
                    for (int i = 0; i < length; i++) {
                        UserInfo userInfo = new UserInfo();
                        userInfo.parse(response.getJSONObject(i));
                        userInfos.add(userInfo);
                    }
                    if(userInfos.size() != 0) {
                        mUserIndexDataHelper.bulkInsert(userInfos);
                        setAdapter(mMentionAdapter);
                        ((Activity)mContext).getLoaderManager().restartLoader(0, null, MentionListPopupWindow.this);
                        mType =TYPE_NORMAL;
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                super.onSuccess(response);
            }
        });
    }

    public void setKeyword(String keyword) {
        DebugUtils.log("keyword: " + keyword);
        mKeyword = keyword;
        if(mType == TYPE_NORMAL){
            ((Activity)mContext).getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mType == TYPE_LOADING){
            //do nothing
        }else if(mType == TYPE_NORMAL){
            if(mOnMentionSelectListener != null) {
                UserIndex userIndex = UserIndex.fromCursor((Cursor) mMentionAdapter.getItem(position));
                mOnMentionSelectListener.onMentionSelect(userIndex.screenName);
            }
            dismiss();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        DebugUtils.log("onCreateLoader");
        return mUserIndexDataHelper.getCursorLoader(UserIndexDBInfo.SEARCH_INDEX + " LIKE '%" + mKeyword + "%'", null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMentionAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMentionAdapter.changeCursor(null);
    }

    private class LoadingAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return new ProgressBar(parent.getContext());
        }
    }
}
