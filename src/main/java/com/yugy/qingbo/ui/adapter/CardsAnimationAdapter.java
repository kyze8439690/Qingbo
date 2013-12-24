package com.yugy.qingbo.ui.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.yugy.qingbo.MyApplication;

/**
 * Created by yugy on 13-12-25.
 */
public class CardsAnimationAdapter extends AnimationAdapter {

    private float mTranslationY = 150;

    private float mRotationX = 8;

    private long mDuration;

    public CardsAnimationAdapter(BaseAdapter baseAdapter) {
        super(baseAdapter);
        mDuration = MyApplication.getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    @Override
    protected long getAnimationDelayMillis() {
        return 30;
    }

    @Override
    protected long getAnimationDurationMillis() {
        return mDuration;
    }

    @Override
    public Animator[] getAnimators(ViewGroup viewGroup, View view) {
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "translationY", mTranslationY, 0),
                ObjectAnimator.ofFloat(view, "RotationX", mRotationX, 0)
        };
    }
}
