package me.yugy.qingbo.view.image;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by yugy on 2014/4/26.
 */
public class CoverImageView extends ImageView{
    public CoverImageView(Context context) {
        super(context);
    }

    public CoverImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoverImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private int mCurrentTranslation = 0;

    public void setCurrentTranslation(int currentTranslation) {
//        DebugUtils.log("setCurrentTranslation: " + currentTranslation);
        mCurrentTranslation = currentTranslation;
//        float ratio =  -mCurrentTranslation / (float)getHeight();
//        int color = Color.argb((int) (128 * ratio), 0, 0, 0);
//        setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(0, -mCurrentTranslation / 2);
        super.draw(canvas);
        canvas.restore();
    }
}
