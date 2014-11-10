package me.yugy.qingbo.view.image;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.makeramen.RoundedImageView;
import me.yugy.qingbo.R;

/**
 * Created by yugy on 13-12-27.
 */
public class HeadIconImageView extends RoundedImageView {

    private Drawable mForegroundSelector;

    public HeadIconImageView(Context context){
        this(context, null);
    }

    public HeadIconImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
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
