package me.yugy.qingbo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import org.json.JSONException;
import org.json.JSONObject;

import me.yugy.qingbo.R;
import me.yugy.qingbo.adapter.TestAdapter;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.type.UserInfo;
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
public class UserActivity extends Activity implements OnRefreshListener {

    private UserInfoDataHelper mUserInfoDataHelper;
    private UserInfo mUserInfo = null;
    private UserHeaderViewHelper mUserHeaderViewHelper;

    private PullToRefreshLayout mPullToRefreshLayout;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        mListView = (ListView) findViewById(R.id.list);

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
        getActionBar().setTitle(userName);
        mUserInfo = mUserInfoDataHelper.select(userName);

        if(mUserInfo == null){
            getUserInfo(userName);
        }else{
            showUserInfo();
        }
    }

    private void getUserInfo(String userName){
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
                super.onSuccess(response);
            }
        });
    }

    private void showUserInfo(){
        mListView.addHeaderView(mUserHeaderViewHelper.getHeaderView(mUserInfo), null, false);
        mListView.setAdapter(new TestAdapter(this));
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true, mUserHeaderViewHelper.getOnScrollListener()));
    }

    @Override
    public void onRefreshStarted(View view) {

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
}
