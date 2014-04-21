package me.yugy.qingbo.view.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import me.yugy.qingbo.R;

/**
 * Created by yugy on 13-12-26.
 */
public class GifIconImageView extends ImageView {
    public GifIconImageView(Context context) {
        super(context);
    }

    public GifIconImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GifIconImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean mIsGif = false;
    private Bitmap gifIcon = null;
    private Paint mPaint = new Paint(){{
        this.setColor(Color.BLACK);
        this.setAntiAlias(true);
    }};

    public void setGif(boolean isGif) {
        mIsGif = isGif;
        if(mIsGif){
            gifIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_gif);
        }else{
            if(gifIcon != null){
                gifIcon.recycle();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mIsGif){
            int left = (canvas.getWidth() - gifIcon.getWidth()) / 2;
            int top = (canvas.getHeight() - gifIcon.getHeight()) / 2;
            canvas.drawBitmap(gifIcon, left, top, mPaint);
        }
    }
}

