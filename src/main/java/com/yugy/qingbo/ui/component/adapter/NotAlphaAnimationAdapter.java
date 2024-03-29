package com.yugy.qingbo.ui.component.adapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.os.Build;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

/**
 * Created by yugy on 13-12-25.
 */
public abstract class NotAlphaAnimationAdapter extends BaseAdapterDecorator {

    protected static final long DEFAULTANIMATIONDELAYMILLIS = 100;
    protected static final long DEFAULTANIMATIONDURATIONMILLIS = 300;
    private static final long INITIALDELAYMILLIS = 150;

    private SparseArray<Animator> mAnimators;
    private long mAnimationStartMillis;
    private int mFirstAnimatedPosition;
    private int mLastAnimatedPosition;
    private boolean mHasParentAnimationAdapter;
    private boolean mShouldAnimate = true;

    private long mInitialDelayMillis = INITIALDELAYMILLIS;
    private long mAnimationDelayMillis = DEFAULTANIMATIONDELAYMILLIS;
    private long mAnimationDurationMillis = DEFAULTANIMATIONDURATIONMILLIS;

    public NotAlphaAnimationAdapter(BaseAdapter baseAdapter) {
        super(baseAdapter);
        mAnimators = new SparseArray<Animator>();

        mAnimationStartMillis = -1;
        mLastAnimatedPosition = -1;

        if (baseAdapter instanceof NotAlphaAnimationAdapter) {
            ((NotAlphaAnimationAdapter) baseAdapter).setHasParentAnimationAdapter(true);
        }
    }

    /**
     * Call this method to reset animation status on all views. The next time
     * {@link #notifyDataSetChanged()} is called on the base adapter, all views will
     * animate again. Will also call {@link #setShouldAnimate(boolean)} with a value of true.
     */
    public void reset() {
        mAnimators.clear();
        mFirstAnimatedPosition = 0;
        mLastAnimatedPosition = -1;
        mAnimationStartMillis = -1;
        mShouldAnimate = true;

        if (getDecoratedBaseAdapter() instanceof NotAlphaAnimationAdapter) {
            ((NotAlphaAnimationAdapter) getDecoratedBaseAdapter()).reset();
        }
    }

    /**
     * Set whether to animate the {@link View}s or not.
     * @param shouldAnimate true if the Views should be animated.
     */
    public void setShouldAnimate(boolean shouldAnimate) {
        mShouldAnimate = shouldAnimate;
    }

    /**
     * Set the starting position for which items should animate. Given position will animate as well.
     * Will also call setShouldAnimate(true).
     * @param position the position.
     */
    public void setShouldAnimateFromPosition(int position) {
        mShouldAnimate = true;
        mFirstAnimatedPosition = position - 1;
        mLastAnimatedPosition = position - 1;
    }

    /**
     * Set the starting position for which items should animate as the first position which isn't currently visible on screen.
     * This call is also valid when the {@link View}s haven't been drawn yet.
     * Will also call setShouldAnimate(true).
     */
    public void setShouldAnimateNotVisible() {
        if (getAbsListView() == null) {
            throw new IllegalStateException("Call setListView() on this AnimationAdapter before setShouldAnimateNotVisible()!");
        }

        mShouldAnimate = true;
        mFirstAnimatedPosition = getAbsListView().getLastVisiblePosition();
        mLastAnimatedPosition = getAbsListView().getLastVisiblePosition();
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (!mHasParentAnimationAdapter) {
            if (getAbsListView() == null) {
                throw new IllegalStateException("Call setListView() on this AnimationAdapter before setAdapter()!");
            }

            if (convertView != null) {
                cancelExistingAnimation(position, convertView);
            }
        }

        View itemView = super.getView(position, convertView, parent);

        if (!mHasParentAnimationAdapter) {
            animateViewIfNecessary(position, itemView, parent);
        }
        return itemView;
    }

    private void cancelExistingAnimation(int position, View convertView) {
        int hashCode = convertView.hashCode();
        Animator animator = mAnimators.get(hashCode);
        if (animator != null) {
            animator.end();
            mAnimators.remove(hashCode);
        }
    }

    private void animateViewIfNecessary(int position, View view, ViewGroup parent) {
        boolean isMeasuringGridViewItem = parent.getHeight() == 0;

        if (position > mLastAnimatedPosition && mShouldAnimate && !isMeasuringGridViewItem) {
            animateView(position, parent, view);
            mLastAnimatedPosition = position;
        }
    }

    private void animateView(int position, ViewGroup parent, View view) {
        if (mAnimationStartMillis == -1) {
            mAnimationStartMillis = System.currentTimeMillis();
        }

        Animator[] childAnimators;
        if (mDecoratedBaseAdapter instanceof NotAlphaAnimationAdapter) {
            childAnimators = ((NotAlphaAnimationAdapter) mDecoratedBaseAdapter).getAnimators(parent, view);
        } else {
            childAnimators = new Animator[0];
        }
        Animator[] animators = getAnimators(parent, view);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(concatAnimators(childAnimators, animators));
        set.setStartDelay(calculateAnimationDelay());
        set.setDuration(getAnimationDurationMillis());
        set.start();

        mAnimators.put(view.hashCode(), set);
    }

    private Animator[] concatAnimators(Animator[] childAnimators, Animator[] animators) {
        Animator[] allAnimators = new Animator[childAnimators.length + animators.length];
        int i;

        for (i = 0; i < animators.length; ++i) {
            allAnimators[i] = animators[i];
        }

        for (int j = 0; j < childAnimators.length; ++j) {
            allAnimators[i] = childAnimators[j];
            ++i;
        }

        return allAnimators;
    }

    @SuppressLint("NewApi")
    private long calculateAnimationDelay() {
        long delay;
        int numberOfItems = getAbsListView().getLastVisiblePosition() - getAbsListView().getFirstVisiblePosition();
        if (numberOfItems + 1 < mLastAnimatedPosition) {
            delay = getAnimationDelayMillis();

            if (getAbsListView() instanceof GridView && Build.VERSION.SDK_INT >= 11) {
                delay += getAnimationDelayMillis() * ((mLastAnimatedPosition + 1) % ((GridView) getAbsListView()).getNumColumns());
            }
        } else {
            long delaySinceStart = (mLastAnimatedPosition - mFirstAnimatedPosition + 1) * getAnimationDelayMillis();
            delay = mAnimationStartMillis + getInitialDelayMillis() + delaySinceStart - System.currentTimeMillis();
        }
        return Math.max(0, delay);
    }

    /**
     * Set whether this AnimationAdapter is encapsulated by another
     * AnimationAdapter. When this is set to true, this AnimationAdapter does
     * not apply any animations to the views. Should not be set explicitly, the
     * AnimationAdapter class manages this by itself.
     */
    public void setHasParentAnimationAdapter(boolean hasParentAnimationAdapter) {
        mHasParentAnimationAdapter = hasParentAnimationAdapter;
    }

    /**
     * Get the delay in milliseconds before the first animation should start. Defaults to {@value #INITIALDELAYMILLIS}.
     */
    protected long getInitialDelayMillis() {
        return mInitialDelayMillis;
    }

    /**
     * Set the delay in milliseconds before the first animation should start. Defaults to {@value #INITIALDELAYMILLIS}.
     * @param delayMillis the time in milliseconds.
     */
    public void setInitialDelayMillis(long delayMillis) {
        mInitialDelayMillis = delayMillis;
    }

    /**
     * Get the delay in milliseconds before an animation of a view should start. Defaults to {@value #DEFAULTANIMATIONDELAYMILLIS}.
     */
    protected long getAnimationDelayMillis() {
        return mAnimationDelayMillis;
    }

    /**
     * Set the delay in milliseconds before an animation of a view should start. Defaults to {@value #DEFAULTANIMATIONDELAYMILLIS}.
     * @param delayMillis the time in milliseconds.
     */
    public void setAnimationDelayMillis(long delayMillis) {
        mAnimationDelayMillis = delayMillis;
    }

    /**
     * Get the duration of the animation in milliseconds. Defaults to {@value #DEFAULTANIMATIONDURATIONMILLIS}.
     */
    protected long getAnimationDurationMillis() {
        return mAnimationDurationMillis;
    }

    /**
     * Set the duration of the animation in milliseconds. Defaults to {@value #DEFAULTANIMATIONDURATIONMILLIS}.
     * @param durationMillis the time in milliseconds.
     */
    public void setAnimationDurationMillis(long durationMillis) {
        mAnimationDurationMillis = durationMillis;
    }

    /**
     * Get the Animators to apply to the views. In addition to the returned
     * Animators, an alpha transition will be applied to the view.
     *
     * @param parent
     *            The parent of the view
     * @param view
     *            The view that will be animated, as retrieved by getView()
     */
    public abstract Animator[] getAnimators(ViewGroup parent, View view);
}