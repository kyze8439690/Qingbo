package me.yugy.qingbo.view.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.ScreenUtils;

/**
 * Created by yugy on 13-12-26.
 */
public class GifIconImageView extends ImageView {
    public GifIconImageView(Context context) {
        this(context, null);
    }

    public GifIconImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifIconImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mWidth = ScreenUtils.dp(getContext(), 32);
    }

    private boolean mIsGif = false;
    private int mWidth;
    private int mLeft, mTop;
    private Drawable mIcon = null;

    public void setGif(boolean isGif) {
        mIsGif = isGif;
        if(mIsGif){
            if(mIcon == null) {
                mIcon = getResources().getDrawable(R.drawable.ic_gif);
            }
            mIcon.setBounds(mLeft, mTop, mLeft + mWidth, mTop + mWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mIsGif){
            mIcon.draw(canvas);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mLeft = (w - mWidth) / 2;
        mTop = (h - mWidth) / 2;
        if(mIcon != null){
            mIcon.setBounds(mLeft, mTop, mLeft + mWidth, mTop + mWidth);
        }
    }
}

