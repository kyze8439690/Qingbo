package com.yugy.qingbo.ui.fragment;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yugy.qingbo.R;
import com.yugy.qingbo.model.CommentModel;
import com.yugy.qingbo.model.RepostModel;
import com.yugy.qingbo.model.TimeLineModel;
import com.yugy.qingbo.sdk.Weibo;
import com.yugy.qingbo.ui.activity.DetailActivity;
import com.yugy.qingbo.ui.component.adapter.CommentListAdapter;
import com.yugy.qingbo.ui.component.adapter.GridPicsAdapter;
import com.yugy.qingbo.ui.component.adapter.RepostListAdapter;
import com.yugy.qingbo.ui.view.AppMsg;
import com.yugy.qingbo.ui.view.HeadIconImageView;
import com.yugy.qingbo.ui.view.LoadMoreView;
import com.yugy.qingbo.ui.view.NoScrollGridView;
import com.yugy.qingbo.ui.view.SelectorImageView;
import com.yugy.qingbo.ui.view.SlidingUpPanelLayout;
import com.yugy.qingbo.utils.NetworkUtils;
import com.yugy.qingbo.utils.ScreenUtils;
import com.yugy.qingbo.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by yugy on 13-12-29.
 */
public class DetailFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener{

    private static final int TAB_TYPE_COMMENT = 0;
    private static final int TAB_TYPE_REPOST = 1;

    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private ViewPager mViewPager;

    private ListView mListView;
    private HeadIconImageView mHead;
    private TextView mName;
    private TextView mText;
    private TextView mRepostName;
    private TextView mRepostText;
    private SelectorImageView mImage;
    private NoScrollGridView mGridView;
    private View mLine;
    private RelativeLayout mHeadLayout;
    private LoadMoreView mLoadMoreView;
    private View mSticky;
    private TextView mStickyComment;
    private TextView mStickyRepost;
    private TextView mNoStickyComment;
    private TextView mNoStickyRepost;

    private CommentListAdapter mCommentListAdapter;
    private RepostListAdapter mRepostListAdapter;
    private TimeLineModel mData;

    private int mTabType = TAB_TYPE_COMMENT;
    private String mSinceCommentId = "0";
    private String mMaxCommentId = "0";
    private String mSinceRepostId = "0";
    private String mMaxRepostId = "0";
    private boolean mIsCommentLoaded = false;
    private boolean mIsRepsotLoaded = false;

    public DetailFragment(SlidingUpPanelLayout slidingUpPanelLayout, ViewPager viewPager){
        mSlidingUpPanelLayout = slidingUpPanelLayout;
        mViewPager = viewPager;
    }

    private int mDelta;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_front, container, false);
        mListView = (ListView) rootView.findViewById(R.id.detail_front_list);
        mSticky = rootView.findViewById(R.id.detail_front_sticky);
        mStickyComment = (TextView) mSticky.findViewById(R.id.detail_sticky_comment);
        mStickyRepost = (TextView) mSticky.findViewById(R.id.detail_sticky_repost);
        final View headerView = inflater.inflate(R.layout.widget_detail_header, null);
        headerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int tabsHeight = ScreenUtils.dp(getActivity(), 48);
                mDelta = headerView.getHeight() - tabsHeight;
            }
        });
        mListView.addHeaderView(headerView);
        mNoStickyComment = (TextView) headerView.findViewById(R.id.detail_sticky_comment);
        mNoStickyRepost = (TextView) headerView.findViewById(R.id.detail_sticky_repost);
        mStickyComment.setOnClickListener(this);
        mStickyRepost.setOnClickListener(this);
        mNoStickyComment.setOnClickListener(this);
        mNoStickyRepost.setOnClickListener(this);
        mHead = (HeadIconImageView) headerView.findViewById(R.id.detail_head);
        mName = (TextView) headerView.findViewById(R.id.detail_name);
        mText = (TextView) headerView.findViewById(R.id.detail_text);
        mText.setMovementMethod(LinkMovementMethod.getInstance());
        mRepostName = (TextView) headerView.findViewById(R.id.detail_repost_name);
        mRepostName.setMovementMethod(LinkMovementMethod.getInstance());
        mRepostText = (TextView) headerView.findViewById(R.id.detail_repost_text);
        mRepostText.setMovementMethod(LinkMovementMethod.getInstance());
        mImage = (SelectorImageView) headerView.findViewById(R.id.detail_pic);
        mImage.setOnClickListener(this);
        mGridView = (NoScrollGridView) headerView.findViewById(R.id.detail_grid);
        mGridView.setOnItemClickListener(this);
        mLine = headerView.findViewById(R.id.detail_frontlayout_divider);
        mLoadMoreView = new LoadMoreView(getActivity());
        mLoadMoreView.setLoadMoreOnClickListener(this);
        mListView.addFooterView(mLoadMoreView);
        mListView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true,
                new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == 0) return;

                int top = headerView.getTop();
                int translationY = Math.max(0, mDelta + top);
                if(translationY == 0){
                    mSticky.setVisibility(View.VISIBLE);
                }else if(firstVisibleItem != 0){
                    mSticky.setVisibility(View.VISIBLE);
                }else{
                    mSticky.setVisibility(View.GONE);
                }
            }
        }));

        mHeadLayout = (RelativeLayout) headerView.findViewById(R.id.detail_frontlayout_head_layout);
        mSlidingUpPanelLayout.setDragView(mHeadLayout);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mData = getArguments().getParcelable(DetailActivity.DATA);
        switch (getArguments().getInt(DetailActivity.VIEW_TYPE, -1)){
            case DetailActivity.VIEW_TYPE_PIC:
                break;
            case DetailActivity.VIEW_TYPE_CONTENT:
                setTextColor(Color.BLACK);
                setPanelColor(Color.WHITE);
                loadComments();
                break;
        }
        ImageLoader.getInstance().displayImage(mData.headUrl, mHead, new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.default_head)
                .showImageForEmptyUri(R.drawable.default_head)
                .showImageForEmptyUri(R.drawable.default_head)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new FadeInBitmapDisplayer(600))
                .build());
        mName.setText(mData.name);
        mText.setText(mData.text);
        setCommentCount(mData.commentCount);
        setRepostCount(mData.repostCount);
        if(mData.hasRepost){
            mLine.setVisibility(View.VISIBLE);
            mRepostName.setVisibility(View.VISIBLE);
            mRepostText.setVisibility(View.VISIBLE);
            mRepostName.setText(mData.repostName);
            mRepostText.setText(mData.repostText);
        }
        if(mData.hasPic || mData.hasRepostPic){
            mImage.setVisibility(View.VISIBLE);
            mImage.setGif(TextUtils.isGifLink(mData.pics.get(0)));
            if(NetworkUtils.isWifi(getActivity())){
                ImageLoader.getInstance().displayImage(mData.pics.get(0).replace("thumbnail", "bmiddle"), mImage);
            }else{
                ImageLoader.getInstance().displayImage(mData.pics.get(0), mImage);
            }
        }
        if(mData.hasPics || mData.hasRepostPics){
            mGridView.setVisibility(View.VISIBLE);
            mGridView.setAdapter(new GridPicsAdapter(getActivity(), mData.pics));
        }

        mCommentListAdapter = new CommentListAdapter(getActivity());
        mRepostListAdapter = new RepostListAdapter(getActivity());
        mListView.setAdapter(mCommentListAdapter);
        super.onActivityCreated(savedInstanceState);
    }

    private void setCommentCount(int count){
        mStickyComment.setText("评论(" + count + ")");
        mNoStickyComment.setText("评论(" + count + ")");
    }

    private void setRepostCount(int count){
        mStickyRepost.setText("转发(" + count + ")");
        mNoStickyRepost.setText("转发(" + count + ")");
    }

    private void setCommentBackgroundResource(int resId){
        mStickyComment.setBackgroundResource(resId);
        mNoStickyComment.setBackgroundResource(resId);
    }

    private void setRepostBackgroundResource(int resId){
        mStickyRepost.setBackgroundResource(resId);
        mNoStickyRepost.setBackgroundResource(resId);
    }

    public void setTextColor(int fontColor){
        mName.setTextColor(fontColor);
        mText.setTextColor(fontColor);
        mRepostName.setTextColor(fontColor);
        mRepostText.setTextColor(fontColor);
    }

    public void setHeaderLayoutBackground(int resourceId){
        mHeadLayout.setBackgroundResource(resourceId);
    }

    public void setPanelColor(int color){
        mHeadLayout.setBackgroundColor(Color.TRANSPARENT);
        mListView.setBackgroundColor(color);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.detail_pic:
                togglePanel();
                break;
            case R.id.loadmore_loadmore_text:
                if(mTabType == TAB_TYPE_COMMENT){
                    mIsCommentLoaded = false;
                    loadComments();
                }else if(mTabType == TAB_TYPE_REPOST){
                    mIsRepsotLoaded = false;
                    loadReposts();
                }
                break;
            case R.id.detail_sticky_comment:
                if(mTabType == TAB_TYPE_REPOST){
                    setTabTypeComment();
                }
                break;
            case R.id.detail_sticky_repost:
                if(mTabType == TAB_TYPE_COMMENT){
                    setTabTypeRepost();
                }
                break;
        }
    }

    private void setTabTypeComment(){
        mLoadMoreView.setVisibility(View.VISIBLE);
        setRepostBackgroundResource(R.drawable.tab_selector_normal);
        setCommentBackgroundResource(R.drawable.tab_selector_selected);
        mTabType = TAB_TYPE_COMMENT;
        mListView.setAdapter(mCommentListAdapter);
        loadComments();
        mListView.setSelection(1);
    }

    private void setTabTypeRepost(){
        mLoadMoreView.setVisibility(View.VISIBLE);
        setCommentBackgroundResource(R.drawable.tab_selector_normal);
        setRepostBackgroundResource(R.drawable.tab_selector_selected);
        mTabType = TAB_TYPE_REPOST;
        mListView.setAdapter(mRepostListAdapter);
        loadReposts();
        mListView.setSelection(1);
    }

    private void togglePanel(){
        if(mSlidingUpPanelLayout.isExpanded() && (mData.hasPic || mData.hasRepostPic || mData.hasPics || mData.hasRepostPics)){
            mSlidingUpPanelLayout.collapsePane();
        }else{
            mSlidingUpPanelLayout.expandPane();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position < mViewPager.getAdapter().getCount()){
            togglePanel();
            mViewPager.setCurrentItem(position);
        }
    }

    public void loadComments(){
        if(!mIsCommentLoaded){
            mLoadMoreView.setType(LoadMoreView.TYPE_LOADING);
            Weibo.getComments(getActivity(), mData.id, mSinceCommentId, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        //parse comment data
                        JSONArray comments = response.getJSONArray("comments");
                        for (int i = 0; i < comments.length(); i++) {
                            CommentModel data = new CommentModel();
                            if (i == 0) {
                                mMaxCommentId = comments.getJSONObject(i).getString("id");
                            }else if(i == comments.length() - 1){
                                mSinceCommentId = comments.getJSONObject(i).getString("id");
                            }
                            data.parse(comments.getJSONObject(i));
                            mCommentListAdapter.getData().add(data);
                        }
                        //check whether has next page
                        int amount = mCommentListAdapter.getData().size();
                        int totalAmount = response.getInt("total_number");
                        setCommentCount(totalAmount);
                        if(totalAmount > amount){
                            mLoadMoreView.setType(LoadMoreView.TYPE_CLICK_TO_LOAD_MORE);
                        }else{
                            mLoadMoreView.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        AppMsg.makeText(getActivity(), "JSON解析错误", AppMsg.STYLE_ALERT).show();
                        e.printStackTrace();
                    } catch (ParseException e) {
                        AppMsg.makeText(getActivity(), "时间解析错误", AppMsg.STYLE_ALERT).show();
                        e.printStackTrace();
                    }
                    mCommentListAdapter.notifyDataSetChanged();
                    mIsCommentLoaded = true;
                    super.onSuccess(response);
                }
            });
        }
    }

    public void loadReposts(){
        if(!mIsRepsotLoaded){
            mLoadMoreView.setType(LoadMoreView.TYPE_LOADING);
            Weibo.getReposts(getActivity(), mData.id, mSinceRepostId, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        //parse comment data
                        JSONArray reposts = response.getJSONArray("reposts");
                        for (int i = 0; i < reposts.length(); i++) {
                            RepostModel data = new RepostModel();
                            if (i == 0) {
                                mMaxRepostId = reposts.getJSONObject(i).getString("id");
                            }else if(i == reposts.length() - 1){
                                mSinceRepostId = reposts.getJSONObject(i).getString("id");
                            }
                            data.parse(reposts.getJSONObject(i));
                            mRepostListAdapter.getData().add(data);
                        }
                        //check whether has next page
                        int amount = mRepostListAdapter.getData().size();
                        int totalAmount = response.getInt("total_number");
                        setRepostCount(totalAmount);
                        if(totalAmount > amount){
                            mLoadMoreView.setType(LoadMoreView.TYPE_CLICK_TO_LOAD_MORE);
                        }else{
                            mLoadMoreView.setVisibility(View.GONE);
                        }
                    } catch (JSONException e) {
                        AppMsg.makeText(getActivity(), "JSON解析错误", AppMsg.STYLE_ALERT).show();
                        e.printStackTrace();
                    } catch (ParseException e) {
                        AppMsg.makeText(getActivity(), "时间解析错误", AppMsg.STYLE_ALERT).show();
                        e.printStackTrace();
                    }
                    mRepostListAdapter.notifyDataSetChanged();
                    mIsRepsotLoaded = true;
                    super.onSuccess(response);
                }
            });
        }
    }
}
