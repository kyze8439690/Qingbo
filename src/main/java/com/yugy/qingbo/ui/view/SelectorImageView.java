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
    }

    public SelectorImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectorImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN && isEnabled()){
            setColorFilter(Color.parseColor("#b8e6ff"), PorterDuff.Mode.MULTIPLY);
        }else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
            setColorFilter(null);
        }
        return super.onTouchEvent(event);
    }
}
