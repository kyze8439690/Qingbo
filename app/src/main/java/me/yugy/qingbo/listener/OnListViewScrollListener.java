package me.yugy.qingbo.listener;

import android.view.View;
import android.widget.AbsListView;

/**
 * Created by yugy on 2014/4/17.
 */
public abstract class OnListViewScrollListener implements AbsListView.OnScrollListener{

    private static final int SCROLL_TO_TOP = -1;
    private static final int SCROLL_TO_BOTTOM = 1;
    private static final int SCROLL_DIRECTION_CHANGE_THRESHOLD = 5;

    private int mScrollPosition;
    private int mScrollDirection = 0;

    private int mScrollState = SCROLL_STATE_IDLE;

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        View topChild = view.getChildAt(0);

        int newScrollPosition;
        if(topChild == null) {
            newScrollPosition = 0;
        } else {
            newScrollPosition = - topChild.getTop() + view.getFirstVisiblePosition() * topChild.getHeight();
        }

        if(Math.abs(newScrollPosition - mScrollPosition) >= SCROLL_DIRECTION_CHANGE_THRESHOLD && mScrollState == SCROLL_STATE_TOUCH_SCROLL) {
            onScrollPositionChanged(mScrollPosition, newScrollPosition);
        }
        mScrollPosition = newScrollPosition;
    }

    private void onScrollPositionChanged(int oldScrollPosition, int newScrollPosition) {
        int newScrollDirection;

        if(newScrollPosition < oldScrollPosition) {
            newScrollDirection = SCROLL_TO_TOP;
        } else {
            newScrollDirection = SCROLL_TO_BOTTOM;
        }

        if(newScrollDirection != mScrollDirection) {
            mScrollDirection = newScrollDirection;
            switch (mScrollDirection){
                case SCROLL_TO_TOP:
                    scrollToTop();
                    break;
                case SCROLL_TO_BOTTOM:
                    scrollToBottom();
                    break;
            }
        }
    }

    public abstract void scrollToTop();

    public abstract void scrollToBottom();


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mScrollState = scrollState;
    }
}
