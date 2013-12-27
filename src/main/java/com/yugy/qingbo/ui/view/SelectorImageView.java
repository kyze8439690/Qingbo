package com.yugy.qingbo.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.yugy.qingbo.R;

/**
 * Created by yugy on 13-11-7.
 */
public class SelectorImageView extends GifIconImageView{
    public SelectorImageView(Context context) {
        super(context);
        init();
    }

    public SelectorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelectorImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Drawable mForegroundSelector;

    private void init(){
        mForegroundSelector = getResources().getDrawable(R.drawable.list_selector_holo);
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
