package me.yugy.qingbo.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import me.yugy.qingbo.utils.DebugUtils;

/**
 * Created by yugy on 2014/7/27.See <a href="https://github.com/Sefford/CircularProgressDrawable/blob/master/circular-progress-drawable%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsefford%2Fcircularprogressdrawable%2FCircularProgressDrawable.java">https://github.com/Sefford/CircularProgressDrawable/blob/master/circular-progress-drawable%2Fsrc%2Fmain%2Fjava%2Fcom%2Fsefford%2Fcircularprogressdrawable%2FCircularProgressDrawable.java</a>
 */
public class LoadingCircularDrawable extends Drawable{

    private Paint mArcPaint;
    private Paint mPointPaint;
    private int mStrokeWidth;
    private int mStartAngle = -90;
    private int mEndAngle = 180;

    public LoadingCircularDrawable(int strokeWidth){
        mStrokeWidth = strokeWidth;

        mArcPaint = new Paint();
        mArcPaint.setColor(Color.WHITE);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(strokeWidth);

        mPointPaint = new Paint();
        mPointPaint.setColor(Color.RED);
        mPointPaint.setAntiAlias(true);
        mPointPaint.setStyle(Paint.Style.STROKE);
        mPointPaint.setStrokeWidth(strokeWidth / 2);
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();
        int size = bounds.height() > bounds.width() ? bounds.width() : bounds.height();
        int radius = (size - mStrokeWidth) / 2;

        //draw the start circular point
        float[] coordinates = getCoordinatesFromAngle(mStartAngle, radius);
        canvas.drawCircle(coordinates[0], coordinates[1], mStrokeWidth / 2, mPointPaint);

        //draw the arc
        RectF rectF = new RectF(
                mStrokeWidth / 2,
                mStrokeWidth / 2,
                size - mStrokeWidth / 2,
                size - mStrokeWidth / 2
        );
//        canvas.drawArc(rectF, mStartAngle, mEndAngle - mStartAngle, false, mArcPaint);
    }

    private float[] getCoordinatesFromAngle(int angle, int radius){
        float x = (float) (radius + radius * Math.cos(-angle));
        float y = (float) (radius + radius * Math.sin(-angle));
        DebugUtils.log("x: " + x + ", y: " + y);
        return new float[]{x, y};
    }

    @Override
    public void setAlpha(int alpha) {
        mArcPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mArcPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return mArcPaint.getAlpha();
    }
}
