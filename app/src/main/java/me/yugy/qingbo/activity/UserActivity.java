package me.yugy.qingbo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import me.yugy.qingbo.R;
import me.yugy.qingbo.adapter.EmptyAdapter;
import me.yugy.qingbo.adapter.NoStoreTimelineStatusAdapter;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.vendor.Weibo;
import me.yugy.qingbo.view.CustomHeaderTransformer;
import me.yugy.qingbo.view.UserHeaderViewHelper;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by yugy on 2014/4/24.
 */
public class UserActivity extends Activity implements OnRefreshListener, OnLoadMoreListener, AdapterView.OnItemClickListener {

    private UserInfoDataHelper mUserInfoDataHelper;
    private UserInfo mUserInfo = null;
    private UserHeaderViewHelper mUserHeaderViewHelper;

    private PullToRefreshLayout mPullToRefreshLayout;
    private ListView mListView;
    private NoStoreTimelineStatusAdapter mTimelineStatusAdapter;

    private long mSinceId = 0;
    private long mMaxId = 0;
    private boolean mIsLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_user);

        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        mListView = (ListView) findViewById(R.id.list);
        mListView.setEmptyView(findViewById(R.id.empty));
        mListView.setOnItemClickListener(this);

        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(this)
                .options(Options.create()
                        .headerTransformer(new CustomHeaderTransformer())
                        .build())
                .setup(mPullToRefreshLayout);

        mUserInfoDataHelper = new UserInfoDataHelper(this);
        mUserHeaderViewHelper = new UserHeaderViewHelper(this);
        String userName = getIntent().getStringExtra("userName");
        DebugUtils.log("Username:" + userName);
        getActionBar().setTitle(userName);
        mUserInfo = mUserInfoDataHelper.select(userName);

        if(mUserInfo == null){
            getUserInfo(userName);
        }else{
            showUserInfo();
        }
    }

    private void getUserInfo(String userName){
        mIsLoading = true;
        Weibo.getUserInfo(this, userName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    mUserInfo = new UserInfo();
                    mUserInfo.parse(response);
                    showUserInfo();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mIsLoading = false;
                super.onSuccess(response);
            }
        });
    }

    private void showUserInfo(){
        mListView.addHeaderView(mUserHeaderViewHelper.getHeaderView(mUserInfo), null, false);
        mListView.setAdapter(new EmptyAdapter());
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true, mUserHeaderViewHelper.getOnScrollListener()));
        getUserNewTimeline();
    }

    private void getUserNewTimeline(){
        mIsLoading = true;
        mPullToRefreshLayout.setRefreshing(true);
        setProgressBarIndeterminateVisibility(true);
        Weibo.getUserNewTimeline(this, mUserInfo.screenName, mSinceId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    ArrayList<Status> statuses = new ArrayList<Status>();
                    int size = response.length();
                    for (int i = 0; i < size; i++) {
                        Status status = new Status();
                        status.parse(response.getJSONObject(i));
                        statuses.add(status);
                    }
                    if(mSinceId == 0){
                        mTimelineStatusAdapter = new NoStoreTimelineStatusAdapter(UserActivity.this, statuses);
                        mListView.setAdapter(mTimelineStatusAdapter);
                        if(statuses.size() != 0){
                            mMaxId = statuses.get(statuses.size() - 1).id;
                        }
                    }else if(statuses.size() != 0){
                        mTimelineStatusAdapter.addNewStatuses(statuses);
                        mSinceId = statuses.get(0).id;
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mPullToRefreshLayout.setRefreshComplete();
                setProgressBarIndeterminateVisibility(false);
                mIsLoading = false;
                super.onSuccess(response);
            }
        });
    }

    private void getUserOldTimeline(){
        DebugUtils.log("getUserOldTimeline");
        mIsLoading = true;
        setProgressBarIndeterminateVisibility(true);
        Weibo.getUserNewTimeline(this, mUserInfo.screenName, mMaxId, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONArray response) {
                try {
                    ArrayList<Status> statuses = new ArrayList<Status>();
                    int size = response.length();
                    for (int i = 0; i < size; i++) {
                        Status status = new Status();
                        status.parse(response.getJSONObject(i));
                        statuses.add(status);
                    }
                    if(statuses.size() != 0){
                        mTimelineStatusAdapter.addOldStatuses(statuses);
                        mMaxId = statuses.get(statuses.size() - 1).id;
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                mIsLoading = false;
                setProgressBarIndeterminateVisibility(false);
                super.onSuccess(response);
            }
        });
    }

    @Override
    public void onRefreshStarted(View view) {
        if(mIsLoading){
            mPullToRefreshLayout.setRefreshComplete();
        }else {
            getUserNewTimeline();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLoadMore() {
        if(!mIsLoading){
            getUserOldTimeline();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(parent.getContext(), NoStoreDetailActivity.class);
        intent.putExtra("status", mTimelineStatusAdapter.getItem(position));
        parent.getContext().startActivity(intent);
    }
}
