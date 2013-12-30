package com.yugy.qingbo.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.makeramen.RoundedImageView;
import com.yugy.qingbo.R;

import java.lang.ref.WeakReference;

/**
 * Created by yugy on 13-12-27.
 */
public class HeadIconImageView extends RoundedImageView{

    private Drawable mForegroundSelector;

    public HeadIconImageView(Context context) {
        super(context);
        init();
    }

    public HeadIconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HeadIconImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mForegroundSelector = getResources().getDrawable(R.drawable.head_selector);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mForegroundSelector.setState(getDrawableState());
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mForegroundSelector.setBounds(0, 0, w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mForegroundSelector.draw(canvas);
    }
}
