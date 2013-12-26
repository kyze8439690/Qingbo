package com.yugy.qingbo.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.yugy.qingbo.R;
import com.yugy.qingbo.Utils.MessageUtil;
import com.yugy.qingbo.Utils.ScreenUtil;
import com.yugy.qingbo.model.TimeLineModel;
import com.yugy.qingbo.sdk.Weibo;
import com.yugy.qingbo.sql.AccountsDataSource;
import com.yugy.qingbo.ui.adapter.CardsAnimationAdapter;
import com.yugy.qingbo.ui.componnet.BottomBarOnScrollListener;
import com.yugy.qingbo.ui.view.AppMsg;
import com.yugy.qingbo.ui.view.TimeLineListItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class MainActivity extends Activity implements ListView.OnItemClickListener,
        OnRefreshListener, View.OnClickListener{

    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawerLeftLayout;
    private RelativeLayout mDrawerRightLayout;
    private ListView mDrawerLeftList;
    private ListView mDrawerRightList;
    private ListView mTimeLineList;
    private ActionBar mActionbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mEmptyView;
    private View mBottomBar;

    private PullToRefreshLayout mPullToRefreshLayout;
    private AccountsDataSource mAccountsDataSource;
    private String[] mDrawerListViewString;
    private ArrayList<TimeLineModel> mTimeLineModels;
    private TimeLineListAdapter mTimeLineListAdapter;
    private AnimationDrawable mJingleDrawable;

    private long firstStatusId = 0;
    private long lastStatusId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobclickAgent.setDebugMode(true);
        MobclickAgent.onError(this);
        UmengUpdateAgent.update(this);
        initViews();
        initComponents();
        if(hasAccount()){
            getNewData();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private boolean hasAccount(){
        mAccountsDataSource = new AccountsDataSource(this);
        mAccountsDataSource.open();
        return mAccountsDataSource.hasAccount();
    }

    private void initViews(){
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawerlayout);
        mActionbar = getActionBar();
        mActionbar.setDisplayHomeAsUpEnabled(true);
        mActionbar.setHomeButtonEnabled(true);
        mActionbar.setIcon(R.drawable.ic_actionbar_icon);
        mActionbar.setTitle("");
        mDrawerLeftLayout = (RelativeLayout) findViewById(R.id.main_left_drawer);
        mDrawerLeftList = (ListView) findViewById(R.id.main_left_drawer_list);
        mDrawerRightLayout = (RelativeLayout) findViewById(R.id.main_right_drawer);
        mDrawerRightList = (ListView) findViewById(R.id.main_right_drawer_list);
        mEmptyView = (TextView) findViewById(R.id.main_right_drawer_emptyview);
        mJingleDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.jingles);
        mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, mJingleDrawable, null, null);
        mEmptyView.setCompoundDrawablePadding(ScreenUtil.dp(this, 10));
        mDrawerRightList.setEmptyView(mEmptyView);

        mTimeLineList = (ListView) findViewById(R.id.main_timeline_list);

        mBottomBar = findViewById(R.id.main_bottombar);
        mBottomBar.setOnClickListener(this);
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        mTimeLineList.addFooterView(progressBar);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.main_refreshlayout);
        ActionBarPullToRefresh.from(this).allChildrenArePullable().listener(this).setup(mPullToRefreshLayout);

        mDrawerListViewString = new String[]{
            "账号",
            "设置"
        };
        mDrawerLeftList.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.widget_drawer_menu_item,
                mDrawerListViewString
        ));
        mDrawerLeftList.setOnItemClickListener(this);
        mDrawerRightList.setOnItemClickListener(this);
        mTimeLineList.setOnItemClickListener(this);

        mDrawerToggle = new TwoDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer_toggle,
                R.string.app_name,
                R.string.app_name
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initComponents(){
        mTimeLineModels = new ArrayList<TimeLineModel>();
        mTimeLineListAdapter = new TimeLineListAdapter();
        CardsAnimationAdapter animationAdapter = new CardsAnimationAdapter(mTimeLineListAdapter);
        animationAdapter.setAbsListView(mTimeLineList);
        mTimeLineList.setAdapter(animationAdapter);



        PauseOnScrollListener pauseOnScrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true, new BottomBarOnScrollListener(mBottomBar));
        mTimeLineList.setOnScrollListener(pauseOnScrollListener);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private static final int ID_HOME = 0x0102002c; //android.R.id.home

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case ID_HOME:
                //close right drawer first if it has been opened.
                if(mDrawerLayout.isDrawerVisible(Gravity.END)){
                    mDrawerLayout.closeDrawer(mDrawerRightLayout);
                }
                //open the left drawer
                if(mDrawerToggle.onOptionsItemSelected(item)){
                    return true;
                }
                break;
            case R.id.main_action_notify:
                //close left drawer first if it has been opened.
                if(mDrawerLayout.isDrawerVisible(Gravity.START)){
                    mDrawerLayout.closeDrawer(mDrawerLeftLayout);
                }
                //toggle the right drawer
                if(mDrawerLayout.isDrawerVisible(Gravity.END)){
                    mDrawerLayout.closeDrawer(mDrawerRightLayout);
                }else{
                    mDrawerLayout.openDrawer(mDrawerRightLayout);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(parent.equals(mDrawerLeftList)){
            switch(position){
                case 0:
                    startActivity(new Intent(MainActivity.this, AccountActivity.class));
                    break;
            }
        }else if(parent.equals(mTimeLineList)){
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivity.DATA, mTimeLineModels.get(position));
            intent.putExtra(DetailActivity.VIEW_TYPE, DetailActivity.VIEW_TYPE_CONTENT);
            startActivity(intent);
        }
    }

    private void getNewData(){
        mPullToRefreshLayout.setRefreshing(true);
        Weibo.getNewTimeline(this, firstStatusId + "", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    TimeLineModel data = new TimeLineModel();
                    try {
                        if (i == 0) {
                            firstStatusId = response.getJSONObject(i).getLong("id");
                        }else if(i == response.length() - 1){
                            lastStatusId = response.getJSONObject(i).getLong("id");
                        }
                        data.parse(response.getJSONObject(i));
                        mTimeLineModels.add(data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                AppMsg.makeText(MainActivity.this, "更新了" + response.length() + "条新微薄", AppMsg.STYLE_INFO).show();
                mPullToRefreshLayout.setRefreshing(false);
                mTimeLineListAdapter.notifyDataSetChanged();
                super.onSuccess(response);
            }
        });
    }

    private void getOldData(){
        mPullToRefreshLayout.setRefreshing(true);
        Weibo.getOldTimeline(this, lastStatusId + "", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONArray response) {
                for (int i = 1; i < response.length(); i++) {
                    TimeLineModel data = new TimeLineModel();
                    try {
                        if(i == response.length() - 1){
                            lastStatusId = response.getJSONObject(i).getLong("id");
                        }
                        data.parse(response.getJSONObject(i));
                        mTimeLineModels.add(mTimeLineModels.size(), data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                AppMsg.makeText(MainActivity.this, "加载了" + (response.length() - 1) + "条微薄", AppMsg.STYLE_INFO).show();
                mTimeLineListAdapter.notifyDataSetChanged();
                mPullToRefreshLayout.setRefreshComplete();
                super.onSuccess(response);
            }
        });
    }

    @Override
    public void onRefreshStarted(View view) {
        Weibo.getNewTimeline(this, firstStatusId + "", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONArray response) {
                for (TimeLineModel mTimeLineModel : mTimeLineModels) {
                    try {
                        mTimeLineModel.reParseTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = response.length() - 1; i >= 0; i--) {
                    TimeLineModel data = new TimeLineModel();
                    try {
                        if (i == 0) {
                            firstStatusId = response.getJSONObject(i).getLong("id");
                        }
                        data.parse(response.getJSONObject(i));
                        mTimeLineModels.add(0, data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                mTimeLineListAdapter.notifyDataSetChanged();
                if (response.length() == 0) {
                    AppMsg.makeText(MainActivity.this, "没有新微博", AppMsg.STYLE_INFO).show();
                } else {
                    AppMsg.makeText(MainActivity.this, "更新了" + response.length() + "条新微薄", AppMsg.STYLE_INFO).show();
                }
                mPullToRefreshLayout.setRefreshComplete();
                super.onSuccess(response);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_bottombar:
                mTimeLineList.setSelectionAfterHeaderView();
                break;
        }
    }

    private class TwoDrawerToggle extends ActionBarDrawerToggle{
        public TwoDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            if(drawerView.equals(mDrawerRightLayout)){
                if(mJingleDrawable.isVisible()){
                    if(mJingleDrawable.isRunning()){
                        mJingleDrawable.stop();
                    }
                    mJingleDrawable.start();
                }
            }else{
                super.onDrawerOpened(drawerView);
            }
            if(mDrawerLayout.isDrawerOpen(Gravity.END) && drawerView.equals(mDrawerLeftLayout)){
                mDrawerLayout.closeDrawer(Gravity.END);
            }else if(mDrawerLayout.isDrawerOpen(Gravity.START) && drawerView.equals(mDrawerRightLayout)){
                mDrawerLayout.closeDrawer(Gravity.START);
            }
        }

        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            if(drawerView == mDrawerLeftLayout){
                super.onDrawerSlide(drawerView, slideOffset);
            }
        }
    }

    private class TimeLineListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mTimeLineModels.size();
        }

        @Override
        public Object getItem(int position) {
            TimeLineListItem item = new TimeLineListItem(MainActivity.this);
            item.parse(mTimeLineModels.get(position));
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TimeLineListItem item;
            if(convertView != null){
                item = (TimeLineListItem) convertView;
            }else{
                item = new TimeLineListItem(MainActivity.this);
            }
            item.parse(mTimeLineModels.get(position));
            if(position == getCount() - 1){
                getOldData();
            }
            return item;
        }
    }
}
