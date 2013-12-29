package com.yugy.qingbo.ui.fragment;

import android.app.ListFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yugy.qingbo.R;
import com.yugy.qingbo.model.CommentModel;
import com.yugy.qingbo.model.TimeLineModel;
import com.yugy.qingbo.sdk.Weibo;
import com.yugy.qingbo.ui.activity.DetailActivity;
import com.yugy.qingbo.ui.component.adapter.CommentListAdapter;
import com.yugy.qingbo.ui.component.adapter.GridPicsAdapter;
import com.yugy.qingbo.ui.view.HeadIconImageView;
import com.yugy.qingbo.ui.view.NoScrollGridView;
import com.yugy.qingbo.ui.view.SelectorImageView;
import com.yugy.qingbo.ui.view.SlidingUpPanelLayout;
import com.yugy.qingbo.utils.NetworkUtils;
import com.yugy.qingbo.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;

/**
 * Created by yugy on 13-12-29.
 */
public class DetailFragment extends ListFragment implements View.OnClickListener, AdapterView.OnItemClickListener{

    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private ViewPager mViewPager;

    private HeadIconImageView mHead;
    private TextView mName;
    private TextView mText;
    private TextView mRepostName;
    private TextView mRepostText;
    private SelectorImageView mImage;
    private NoScrollGridView mGridView;
    private View mLine;
    private RelativeLayout mHeadLayout;

    private CommentListAdapter mCommentListAdapter;
    private TimeLineModel mData;

    private String mSinceCommentId = "0";
    private String mMaxCommentId = "0";
    private boolean mIsCommentLoaded = false;

    public DetailFragment(SlidingUpPanelLayout slidingUpPanelLayout, ViewPager viewPager){
        mSlidingUpPanelLayout = slidingUpPanelLayout;
        mViewPager = viewPager;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        View headerView = View.inflate(getActivity(), R.layout.widget_detail_header, null);
        getListView().addHeaderView(headerView);
//        getListView().setEmptyView(null);
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

        mHeadLayout = (RelativeLayout) headerView.findViewById(R.id.detail_frontlayout_head_layout);
        mSlidingUpPanelLayout.setDragView(mHeadLayout);

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
        getListView().setAdapter(mCommentListAdapter);
        setListShownNoAnimation(true);
        getListView().setOverScrollMode(ListView.OVER_SCROLL_NEVER);
        getListView().addFooterView(new ProgressBar(getActivity()));
        getListView().setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        super.onActivityCreated(savedInstanceState);
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
        getListView().setBackgroundColor(color);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.detail_pic:
                togglePanel();
                break;
        }
    }

    private void togglePanel(){
        if(mSlidingUpPanelLayout.isExpanded() && (mData.hasPic || mData.hasRepostPic)){
            mSlidingUpPanelLayout.collapsePane();
        }else{
            mSlidingUpPanelLayout.expandPane();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position < mViewPager.getChildCount()){
            mViewPager.setCurrentItem(position);
            togglePanel();
        }
    }

    public void loadComments(){
        if(!mIsCommentLoaded){
            Weibo.getComments(getActivity(), mData.id, mSinceCommentId, new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(JSONArray response) {
                    for (int i = 0; i < response.length(); i++) {
                        CommentModel data = new CommentModel();
                        try {
                            if (i == 0) {
                                mMaxCommentId = response.getJSONObject(i).getString("id");
                            }else if(i == response.length() - 1){
                                mSinceCommentId = response.getJSONObject(i).getString("id");
                            }
                            data.parse(response.getJSONObject(i));
                            mCommentListAdapter.getData().add(data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    mCommentListAdapter.notifyDataSetChanged();
                    super.onSuccess(response);
                }
            });
        }
    }
}
