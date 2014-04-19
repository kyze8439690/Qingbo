package me.yugy.qingbo.activity;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;

import me.yugy.qingbo.R;
import me.yugy.qingbo.adapter.TimelineStatusAdapter;
import me.yugy.qingbo.dao.datahelper.StatusesDataHelper;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
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
        View.OnClickListener {

    private PullToRefreshLayout mPullToRefreshLayout;
    private ListView mListView;
    private View mBottomBar;
    private StatusesDataHelper mStatusesDataHelper;
    private TimelineStatusAdapter mTimelineStatusAdapter;
    private OnListViewScrollListener mOnListViewScrollListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_timeline);

        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        mListView = (ListView) findViewById(R.id.list);
        mBottomBar = findViewById(R.id.bottom_bar);

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
        mTimelineStatusAdapter = new TimelineStatusAdapter(this, mOnListViewScrollListener);
        mListView.setAdapter(mTimelineStatusAdapter);

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
        Weibo.getNewTimeline(this, mStatusesDataHelper.getNewestId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(final JSONArray response) {
                DebugUtils.log("解析微博数据");
                ArrayList<Status> statuses = new ArrayList<Status>();
                int statusesSize = response.length();
                if(statusesSize == 20){
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
            case R.id.btn_bottombar_refresh:
                mListView.setSelectionAfterHeaderView();
                getNewData();
                break;
        }
    }
}
