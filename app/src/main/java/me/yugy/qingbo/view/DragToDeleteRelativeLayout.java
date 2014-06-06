package me.yugy.qingbo.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.ScreenUtils;

/**
 * Created by yugy on 2014/6/2.
 */
public class DragToDeleteRelativeLayout extends RelativeLayout{

    private ViewDragHelper mDragHelper;
    private View mDragView;

    private OnSwipeToDeleteListener mOnSwipeToDeleteListener;

    private int mLeftBound;
    private int mRightBound;

    private boolean mIsDragViewShowing = true;

    public DragToDeleteRelativeLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragToDeleteRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
    }

    public interface OnSwipeToDeleteListener{
        public void onSwipeToDelete();
    }

    public void setOnSwipeToDeleteListener(OnSwipeToDeleteListener onSwipeToDeleteListener) {
        mOnSwipeToDeleteListener = onSwipeToDeleteListener;
    }

    private class DragHelperCallback extends ViewDragHelper.Callback{

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragView && mIsDragViewShowing;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            float mDragOffset = (float) (left - mLeftBound) / (mRightBound - mLeftBound);
            mDragView.setAlpha(mDragOffset);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int newLeft = Math.min(Math.max(left, mLeftBound), mRightBound);
            return newLeft;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop();
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mLeftBound;
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            if(xvel < -mDragHelper.getMinVelocity()){
                //swipe to left
                settleAtLeft();
            }else if(xvel > mDragHelper.getMinVelocity()){
                //swipe to right
                settleAtRight();
            }else if(mDragView.getLeft() <= (mLeftBound + mRightBound ) / 2){
                //stop at left
                settleAtLeft();
            }else{
                //stop at right
                settleAtRight();
            }
        }
    }

    private void settleAtLeft(){
        mIsDragViewShowing = false;
        if(mOnSwipeToDeleteListener != null){
            mOnSwipeToDeleteListener.onSwipeToDelete();
        }
        mDragHelper.settleCapturedViewAt(mLeftBound, mDragView.getTop());
        invalidate();
    }

    private void settleAtRight(){
        mIsDragViewShowing = true;
        mDragHelper.settleCapturedViewAt(mRightBound, mDragView.getTop());
        invalidate();
    }

    public void show(){
        mIsDragViewShowing = true;
        mDragView.setAlpha(1);
        mDragView.setLeft(mRightBound);
    }

    public void delete(){
        mIsDragViewShowing = false;
//        mDragView.setAlpha(0);
//        mDragView.setLeft(mLeftBound);
        mDragHelper.smoothSlideViewTo(mDragView, mLeftBound, mDragView.getTop());
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if(mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView = findViewById(R.id.new_status_thumbnail);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mLeftBound = (getWidth() - mDragView.getWidth()) / 2;
        mRightBound = getWidth() - mDragView.getWidth() - ScreenUtils.dp(getContext(), 8);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
            mDragHelper.cancel();
            return false;
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }
}
