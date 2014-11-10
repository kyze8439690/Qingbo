package me.yugy.qingbo.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.yugy.qingbo.R;
import me.yugy.qingbo.fragment.HomeFragment;
import me.yugy.qingbo.view.LoadingCircularDrawable;

/**
 * Created by yugy on 2014/4/16.
 */
public class TimelineActivity extends Activity {

    @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
    @InjectView(R.id.loading) ImageView mLoading;
    private HomeFragment mHomeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_timeline);
        ButterKnife.inject(this);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarAlpha(0);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerLayout.setDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                if(mHomeFragment != null && mHomeFragment.isVisible()) {
                    if (newState == DrawerLayout.STATE_SETTLING) {
                        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                            mHomeFragment.showFloatingAction();
                        } else {
                            mHomeFragment.hideFloatingAction();
                        }
                    } else if (newState == DrawerLayout.STATE_IDLE) {
                        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                            mHomeFragment.hideFloatingAction();
                        } else {
                            mHomeFragment.showFloatingAction();
                        }
                    }
                }
            }
        });

        mLoading.setImageDrawable(new LoadingCircularDrawable(getResources().getDimensionPixelSize(R.dimen.loading_stroke_width)));

        if(savedInstanceState == null){
            if(mHomeFragment == null){
                mHomeFragment = new HomeFragment();
            }
            getFragmentManager().beginTransaction()
                    .add(R.id.container, mHomeFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @OnClick(R.id.btn_drawer) void toggleDrawer(){
        if(mDrawerLayout.isDrawerOpen(Gravity.START)){
            mDrawerLayout.closeDrawer(Gravity.START);
        }else{
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }
}
