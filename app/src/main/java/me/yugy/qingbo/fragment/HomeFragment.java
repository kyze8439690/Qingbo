package me.yugy.qingbo.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;
import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import me.yugy.qingbo.R;
import me.yugy.qingbo.activity.DetailActivity;
import me.yugy.qingbo.activity.NewStatusActivity;
import me.yugy.qingbo.adapter.TimelineStatusAdapter;
import me.yugy.qingbo.broadcast.RefreshTimelineBroadcastReceiver;
import me.yugy.qingbo.dao.datahelper.StatusesDataHelper;
import me.yugy.qingbo.dao.dbinfo.StatusDBInfo;
import me.yugy.qingbo.listener.OnLoadMoreListener;
import me.yugy.qingbo.type.Status;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.vendor.Weibo;
import me.yugy.qingbo.view.ChattyListView;
import me.yugy.qingbo.view.HomeFragmentHeaderTransformer;
import me.yugy.qingbo.view.FloatingAction;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by yugy on 2014/7/27.
 */
public class HomeFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnRefreshListener, OnLoadMoreListener{

    @InjectView(R.id.ptr_layout) PullToRefreshLayout mPullToRefreshLayout;
    @InjectView(R.id.list) ChattyListView mListView;

    private FloatingAction mFloatingAction;
    private StatusesDataHelper mStatusesDataHelper;
    private TimelineStatusAdapter mTimelineStatusAdapter;
    private RefreshTimelineBroadcastReceiver mBroadcastReceiver;

    private boolean mIsBroadcastReceiverRegistered = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusesDataHelper = new StatusesDataHelper(getActivity());
        mTimelineStatusAdapter = new TimelineStatusAdapter(getActivity(), this);
        mBroadcastReceiver = new RefreshTimelineBroadcastReceiver() {
            @Override
            public void onRefresh() {
                getNewData();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFloatingAction = FloatingAction.from(getActivity())
                .listenTo(mListView)
                .icon(R.drawable.ic_pencil)
                .listener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(getActivity(), NewStatusActivity.class));
                    }
                })
                .build();

        ActionBarPullToRefresh.from(getActivity())
                .listener(this)
                .allChildrenArePullable()
                .options(Options.create()
                        .headerTransformer(new HomeFragmentHeaderTransformer())
                        .build())
                .setup(mPullToRefreshLayout);

        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        mListView.setAdapter(mTimelineStatusAdapter);
        getLoaderManager().initLoader(0, null, this);

        if(mStatusesDataHelper.getNewestId() == 0){
            getNewData();
        }
    }

    public void showFloatingAction(){
        if(mFloatingAction != null){
            mFloatingAction.show();
        }
    }

    public void hideFloatingAction(){
        if(mFloatingAction != null){
            mFloatingAction.hide();
        }
    }

    private void getNewData(){
        mPullToRefreshLayout.setRefreshing(true);
//        setProgressBarIndeterminateVisibility(true);
        Weibo.getNewTimeline(getActivity(), mStatusesDataHelper.getNewestId(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                DebugUtils.log("解析微博数据");
                ArrayList<Status> statuses = new ArrayList<Status>();
                int statusesSize = response.length();
                if (statusesSize >= 20) {
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
                if (statuses.size() != 0) {
                    mStatusesDataHelper.bulkInsert(statuses);
                    MessageUtils.toast(getActivity(), "更新了" + statuses.size() + "条新微薄");
                } else {
                    MessageUtils.toast(getActivity(), "没有新微博");
                }
                mPullToRefreshLayout.setRefreshComplete();
//                setProgressBarIndeterminateVisibility(false);
                super.onSuccess(statusCode, headers, response);
            }
        });
    }

    private boolean mLoadingMore = false;

    private void getOldData(){
        mLoadingMore = true;
//        setProgressBarIndeterminateVisibility(true);
        Weibo.getOldTimeline(getActivity(), mStatusesDataHelper.getOldestId(), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
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
                if (statuses.size() != 0) {
                    mStatusesDataHelper.bulkInsert(statuses);
                    MessageUtils.toast(getActivity(), "加载了" + statuses.size() + "条新微薄");
                } else {
                    MessageUtils.toast(getActivity(), "没有新微博");
                }
//                setProgressBarIndeterminateVisibility(false);
                mLoadingMore = false;
                super.onSuccess(statusCode, headers, response);
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
    public void onLoadMore() {
        if(!mLoadingMore){
            getOldData();
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        getNewData();
    }

    @OnItemClick(R.id.list)
    public void onItemClick(ChattyListView parent, int position) {
        try {
            Cursor cursor = (Cursor)parent.getAdapter().getItem(position);
            Status status = Status.fromCursor(cursor);
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("status", status);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void onResume() {
        super.onResume();
        if(!mIsBroadcastReceiverRegistered){
            getActivity().registerReceiver(mBroadcastReceiver, new IntentFilter(RefreshTimelineBroadcastReceiver.ACTION_REFRESH_TIMELINE));
            mIsBroadcastReceiverRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mIsBroadcastReceiverRegistered){
            getActivity().unregisterReceiver(mBroadcastReceiver);
            mIsBroadcastReceiverRegistered = false;
        }
    }

    @Override
    public void onDestroy() {
        mFloatingAction.onDestroy();
        super.onDestroy();
    }
}
