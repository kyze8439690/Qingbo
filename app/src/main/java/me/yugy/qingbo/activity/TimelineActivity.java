package me.yugy.qingbo.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;

import me.yugy.qingbo.BuildConfig;
import me.yugy.qingbo.R;
import me.yugy.qingbo.adapter.TimelineStatusAdapter;
import me.yugy.qingbo.broadcast.RefreshTimelineBroadcastReceiver;
import me.yugy.qingbo.dao.datahelper.StatusesDataHelper;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.debug.ViewServer;
import me.yugy.qingbo.listener.DrawerToggle;
import me.yugy.qingbo.listener.OnListViewScrollListener;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.vendor.Weibo;
import me.yugy.qingbo.view.CustomHeaderTransformer;
import me.yugy.qingbo.view.message.AppMsg;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by yugy on 2014/4/16.
 */
public class TimelineActivity extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor>, OnRefreshListener, OnLoadMoreListener,
        View.OnClickListener, ActionBar.OnNavigationListener, AdapterView.OnItemClickListener {

    private DrawerLayout mDrawerLayout;
    private PullToRefreshLayout mPullToRefreshLayout;
    private ListView mListView;
    private View mBottomBar;
    private DrawerToggle mDrawerToggle;
    private StatusesDataHelper mStatusesDataHelper;
    private TimelineStatusAdapter mTimelineStatusAdapter;
    private OnListViewScrollListener mOnListViewScrollListener;
    private RefreshTimelineBroadcastReceiver mBroadcastReceiver;

    private boolean mIsBroadcastReceiverRegistered = false;

    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        if(BuildConfig.DEBUG) {
            ViewServer.get(this).addWindow(this);
//            StrictMode.enableDefaults();
        }
        setContentView(R.layout.activity_timeline);

        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setListNavigationCallbacks(ArrayAdapter.createFromResource(this, R.array.actionbar_dropdown_entry, android.R.layout.simple_spinner_dropdown_item), this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        mListView = (ListView) findViewById(R.id.list);
        mBottomBar = findViewById(R.id.bottom_bar);
        mDrawerToggle = new DrawerToggle(this, mDrawerLayout);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mStatusesDataHelper = new StatusesDataHelper(this);
        mOnListViewScrollListener = new OnListViewScrollListener() {

            private int mBottomBarHeight = -1;
            private int mTranslationY;

            @Override
            public void scrollToTop() {
                if(mBottomBarHeight <= 0) {
                    mBottomBarHeight = mBottomBar.getHeight();
                }
                mTranslationY = 0;
                mBottomBar.animate().setDuration(300).translationY(mTranslationY);
            }

            @Override
            public void scrollToBottom() {
                if(mBottomBarHeight <= 0) {
                    mBottomBarHeight = mBottomBar.getHeight();
                }
                mTranslationY = mBottomBarHeight;
                mBottomBar.animate().setDuration(300).translationY(mTranslationY);
            }
        };
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true, mOnListViewScrollListener));
        mTimelineStatusAdapter = new TimelineStatusAdapter(this);
        mListView.setAdapter(mTimelineStatusAdapter);
        mListView.setOnItemClickListener(this);
        mBroadcastReceiver = new RefreshTimelineBroadcastReceiver() {
            @Override
            public void onRefresh() {
                getNewData();
            }
        };

        mBottomBar.findViewById(R.id.btn_bottombar_photo).setOnClickListener(this);
        mBottomBar.findViewById(R.id.btn_bottombar_location).setOnClickListener(this);
        mBottomBar.findViewById(R.id.btn_bottombar_text).setOnClickListener(this);
        mBottomBar.findViewById(R.id.btn_bottombar_refresh).setOnClickListener(this);


        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(this)
                .options(Options.create()
                        .headerTransformer(new CustomHeaderTransformer())
                        .build())
                .setup(mPullToRefreshLayout);

        getLoaderManager().initLoader(0, null, this);

        if(mStatusesDataHelper.getNewestId() == 0){
            getNewData();
        }
    }

    private void getNewData(){
        mPullToRefreshLayout.setRefreshing(true);
        setProgressBarIndeterminateVisibility(true);
        Weibo.getNewTimeline(this, mStatusesDataHelper.getNewestId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(final JSONArray response) {
                DebugUtils.log("解析微博数据");
                ArrayList<Status> statuses = new ArrayList<Status>();
                int statusesSize = response.length();
                if(statusesSize >= 20){
                    mStatusesDataHelper.deleteAll();
                }
                try {
                    for (int i = 0; i < statusesSize; i++) {
                        Status status = new Status();
                        status.parse(response.getJSONObject(i));
                        statuses.add(status);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(statuses.size() != 0){
                    mStatusesDataHelper.bulkInsert(statuses);
                    AppMsg.makeText(TimelineActivity.this, "更新了" + statuses.size() + "条新微薄").show();
                } else {
                    AppMsg.makeText(TimelineActivity.this, "没有新微博").show();
                }
                mPullToRefreshLayout.setRefreshComplete();
                setProgressBarIndeterminateVisibility(false);
                super.onSuccess(response);
            }
        });
    }

    private boolean mLoadingMore = false;

    private void getOldData(){
        mLoadingMore = true;
        setProgressBarIndeterminateVisibility(true);
        Weibo.getOldTimeline(this, mStatusesDataHelper.getOldestId(), new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONArray response) {
                DebugUtils.log("解析微博数据");
                ArrayList<Status> statuses = new ArrayList<Status>();
                int statusesSize = response.length();
                for (int i = 0; i < statusesSize; i++) {
                    Status status = new Status();
                    try {
                        status.parse(response.getJSONObject(i));
                        statuses.add(status);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if(statuses.size() != 0){
                    mStatusesDataHelper.bulkInsert(statuses);
                    AppMsg.makeText(TimelineActivity.this, "加载了" + statuses.size() + "条新微薄").show();
                } else {
                    AppMsg.makeText(TimelineActivity.this, "没有新微博").show();
                }
                setProgressBarIndeterminateVisibility(false);
                mLoadingMore = false;
                super.onSuccess(response);
            }
        });
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mStatusesDataHelper.getCursorLoader(StatusDBInfo.ID + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTimelineStatusAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mTimelineStatusAdapter.changeCursor(null);
    }

    @Override
    public void onRefreshStarted(View view) {
        getNewData();
    }

    @Override
    public void onLoadMore() {
        if(!mLoadingMore){
            getOldData();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_bottombar_photo:
                startActivityForResult(new Intent(this, PickPhotoSourceActivity.class), NewStatusActivity.REQUEST_PICK_IMAGE);
                break;
            case R.id.btn_bottombar_location:
                Intent intent = new Intent(this, NewStatusActivity.class);
                intent.putExtra("getLocation", true);
                startActivity(intent);
                break;
            case R.id.btn_bottombar_text:
                startActivity(new Intent(this, NewStatusActivity.class));
                break;
            case R.id.btn_bottombar_refresh:
                mListView.setSelectionAfterHeaderView();
                getNewData();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == NewStatusActivity.REQUEST_PICK_IMAGE && resultCode == RESULT_OK){
            mImagePath = data.getStringExtra("imagePath");
            newPhotoStatus();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void newPhotoStatus(){
        Intent intent = new Intent(this, NewStatusActivity.class);
        intent.putExtra("imagePath", mImagePath);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            Cursor cursor = (Cursor)parent.getAdapter().getItem(position);
            Status status = Status.fromCursor(cursor);
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("status", status);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if(BuildConfig.DEBUG) {
            ViewServer.get(this).removeWindow(this);
        }
    }

    public void onResume() {
        super.onResume();
        if(BuildConfig.DEBUG) {
            ViewServer.get(this).setFocusedWindow(this);
        }
        if(!mIsBroadcastReceiverRegistered){
            registerReceiver(mBroadcastReceiver, new IntentFilter(RefreshTimelineBroadcastReceiver.ACTION_REFRESH_TIMELINE));
            mIsBroadcastReceiverRegistered = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsBroadcastReceiverRegistered){
            unregisterReceiver(mBroadcastReceiver);
            mIsBroadcastReceiverRegistered = false;
        }
    }
}
