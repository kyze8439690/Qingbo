package com.yugy.qingbo.ui.activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.yugy.qingbo.R;
import com.yugy.qingbo.ui.fragment.DetailFragment;
import com.yugy.qingbo.utils.ScreenUtils;
import com.yugy.qingbo.model.TimeLineModel;
import com.yugy.qingbo.ui.fragment.PicFragment;
import com.yugy.qingbo.ui.view.PicViewPager;
import com.yugy.qingbo.ui.view.SlidingUpPanelLayout;

/**
 * Created by yugy on 13-11-7.
 */
public class DetailActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        initViews();
        obtainData();
    }

    private SlidingUpPanelLayout slidingLayout;
    private ActionBar actionBar;
    private PicViewPager viewPager;
    private TextView actionbarTitle;

    private DetailFragment mDetailFragment;

    private TimeLineModel mData;
    private Drawable actionBarBackgroundDrawable;

    public final static String DATA = "data";
    public final static String VIEW_TYPE = "viewType";
    public final static String VIEW_PICS_ITEM_ID = "picId";

    public final static int VIEW_TYPE_CONTENT = 0;
    public final static int VIEW_TYPE_PIC = 1;

    private void obtainData(){
        mData = getIntent().getParcelableExtra(DATA);
        mDetailFragment.setArguments(getIntent().getExtras());
        getFragmentManager().beginTransaction().add(R.id.detail_frontlayout, mDetailFragment).commit();
        switch (getIntent().getIntExtra(VIEW_TYPE, -1)){
            case VIEW_TYPE_PIC:
                displayImage(getIntent().getIntExtra(VIEW_PICS_ITEM_ID, -1));
                actionBar.hide();
                slidingLayout.setPanelHeight(ScreenUtils.dp(this, 48));
                break;
            case VIEW_TYPE_CONTENT:
                actionBarBackgroundDrawable.setAlpha(255);
                actionBarBackgroundDrawable.invalidateSelf();
                actionbarTitle.setTextColor(Color.BLACK);
                slidingLayout.expandPane();
                if(mData.hasPic || mData.hasRepostPic){
                    displayImage(0);
                }else{
                    slidingLayout.setSlidingEnabled(false);
                }
                break;
        }
    }

    private void initViews(){
        actionBar = getActionBar();
        actionBarBackgroundDrawable = getResources().getDrawable(R.drawable.ab_solid_light_holo);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN){
            actionBarBackgroundDrawable.setCallback(mDrawableCallback);
        }
        actionBarBackgroundDrawable.setAlpha(0);
        actionBar.setBackgroundDrawable(actionBarBackgroundDrawable);
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.detail_rootlayout);
        slidingLayout.setPanelOverlayable(true);
        slidingLayout.setPanelTransparent(true);
        slidingLayout.setPanelHeight(ScreenUtils.dp(this, 82 + 48));
        slidingLayout.setEnableDragViewTouchEvents(true);
        slidingLayout.setPanelSlideListener(new SlidingUpPanelLayout.SimplePanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                int alpha = (int) (255 * (1 - slideOffset));
                actionBarBackgroundDrawable.setAlpha(alpha);
                actionBarBackgroundDrawable.invalidateSelf();
                int fontColor;
                int backgroundColor = Color.argb(alpha, 255, 255, 255);
                if (alpha < 128) {
                    fontColor = Color.argb(255 - 2 * alpha, 255, 255, 255);
                } else {
                    fontColor = Color.argb(2 * (alpha - 128), 0, 0, 0);
                }
                actionbarTitle.setTextColor(fontColor);
                mDetailFragment.setTextColor(fontColor);
                mDetailFragment.setPanelColor(backgroundColor);
            }

            @Override
            public void onPanelExpanded(View panel) {
                if (!actionBar.isShowing()) {
                    actionBar.show();
                }
                mDetailFragment.loadComments();
            }

            @Override
            public void onPanelCollapsed(View panel) {
                mDetailFragment.setHeaderLayoutBackground(R.drawable.black_gradient);
                if (slidingLayout.getPanelHeight() == ScreenUtils.dp(DetailActivity.this, 48)) {
                    actionBar.hide();
                }
            }
        });
        viewPager = (PicViewPager) findViewById(R.id.detail_picpager);
        mDetailFragment = new DetailFragment(slidingLayout, viewPager);

        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        actionbarTitle = (TextView) findViewById(titleId);
        actionbarTitle.setTextColor(Color.WHITE);
    }

    private Drawable.Callback mDrawableCallback = new Drawable.Callback() {
        @Override
        public void invalidateDrawable(Drawable who) {
            actionBar.setBackgroundDrawable(who);
        }

        @Override
        public void scheduleDrawable(Drawable who, Runnable what, long when) {}

        @Override
        public void unscheduleDrawable(Drawable who, Runnable what) {}
    };

    private void displayImage(final int index){
        viewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                PicFragment picFragment = new PicFragment(slidingLayout);
                Bundle args = new Bundle();
                args.putString("url", mData.pics.get(i).replace("thumbnail", "large"));
                picFragment.setArguments(args);
                return picFragment;
            }

            @Override
            public int getCount() {
                return mData.pics.size();
            }


        });
        viewPager.setCurrentItem(index);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if(NavUtils.shouldUpRecreateTask(this, upIntent)){
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                }else{
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
