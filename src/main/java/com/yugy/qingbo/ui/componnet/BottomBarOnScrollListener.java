package com.yugy.qingbo.ui.componnet;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.widget.AbsListView;

import com.yugy.qingbo.R;

/**
 * Created by yugy on 13-12-26.
 */
public class BottomBarOnScrollListener implements AbsListView.OnScrollListener {

    public BottomBarOnScrollListener(View mBottomBar) {
        mShowAnimator = ObjectAnimator.ofFloat(mBottomBar, "translationY", 0);
        mShowAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mBottomBarState = BOTTOM_BAR_STATE_SHOWING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mBottomBarState = BOTTOM_BAR_STATE_ON_SCREEN;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mBottomBarState = BOTTOM_BAR_STATE_OFF_SCREEN;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        mHideAnimator = ObjectAnimator.ofFloat(mBottomBar, "translationY", mBottomBar.getResources().getDimension(R.dimen.bottom_height));
        mHideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mBottomBarState = BOTTOM_BAR_STATE_HIDING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mBottomBarState = BOTTOM_BAR_STATE_OFF_SCREEN;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mBottomBarState = BOTTOM_BAR_STATE_ON_SCREEN;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
    }

    private int mLastVisibleItem = 0;
    private int mLastY = 0;
    private ObjectAnimator mShowAnimator;
    private ObjectAnimator mHideAnimator;

    private static final int BOTTOM_BAR_STATE_ON_SCREEN = 0;
    private static final int BOTTOM_BAR_STATE_OFF_SCREEN = 1;
    private static final int BOTTOM_BAR_STATE_SHOWING = 2;
    private static final int BOTTOM_BAR_STATE_HIDING = 3;

    private int mBottomBarState = BOTTOM_BAR_STATE_ON_SCREEN;

    private static final int ACTION_STATE_IDLE = 0;
    private static final int ACTION_STATE_SCROLL_UP = 1; //scroll to up, look below, hide the bottombar
    private static final int ACTION_STATE_SCROLL_DOWN = 2; // scroll to down, look above, show the bottombar

    private int getActionState(int firstVisibleItem, int top){
        if(firstVisibleItem > mLastVisibleItem){
            return ACTION_STATE_SCROLL_UP;
        }else if(firstVisibleItem < mLastVisibleItem){
            return ACTION_STATE_SCROLL_DOWN;
        }else{
            if(top < mLastY){
                return ACTION_STATE_SCROLL_UP;
            }else if(top > mLastY){
                return ACTION_STATE_SCROLL_DOWN;
            }else{
                return ACTION_STATE_IDLE;
            }
        }
    }

    private boolean isBottomBarShowing(){
        return mShowAnimator.isRunning() || mShowAnimator.isStarted();
    }

    private boolean isBottomBarHiding(){
        return mHideAnimator.isRunning() || mHideAnimator.isStarted();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int top = 0;
        if(view.getChildAt(0) != null){
            top = view.getChildAt(0).getTop();
        }

        int actionState = getActionState(firstVisibleItem, top);
        switch (mBottomBarState){
            case BOTTOM_BAR_STATE_OFF_SCREEN:
                if(!isBottomBarShowing() && actionState == ACTION_STATE_SCROLL_DOWN){
                    mShowAnimator.start();
                }
                break;
            case BOTTOM_BAR_STATE_ON_SCREEN:
                if(!isBottomBarHiding() && actionState == ACTION_STATE_SCROLL_UP){
                    mHideAnimator.start();
                }
                break;
            case BOTTOM_BAR_STATE_SHOWING:
                if(actionState == ACTION_STATE_SCROLL_UP){
                    mShowAnimator.cancel();
                    mHideAnimator.start();
                }
                break;
            case BOTTOM_BAR_STATE_HIDING:
                if(actionState == ACTION_STATE_SCROLL_DOWN){
                    mHideAnimator.cancel();
                    mShowAnimator.start();
                }
                break;
        }
        mLastVisibleItem = firstVisibleItem;
        mLastY = top;
    }
}
