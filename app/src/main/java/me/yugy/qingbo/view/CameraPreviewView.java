package me.yugy.qingbo.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.ScreenUtils;

import static android.hardware.Camera.CameraInfo;

/**
 * Created by yugy on 2014/6/4.
 */
public class CameraPreviewView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private Drawable mLogo;
    private Drawable mForegroundSelector;
    private int mLeft, mTop;
    private int mLogoWidth;
    private int mViewWidth;
    private int mTopOffset;
    private Paint mPaint;
    private String mText;
    private int mTextLeft;
    private int mTextTop;

    public CameraPreviewView(Context context) {
        this(context, null);
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLogo = getResources().getDrawable(R.drawable.ic_action_camera);
        mForegroundSelector = getResources().getDrawable(R.drawable.list_selector_holo);
        mLogoWidth = ScreenUtils.dp(context, 32);
        mTopOffset = ScreenUtils.dp(context, 8);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(ScreenUtils.sp(context, 10));
        mText = "Camera";
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mForegroundSelector.setState(getDrawableState());
        invalidate();
    }

    public void init(Camera camera){
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setDisplayOrientation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mLogo.draw(canvas);
        canvas.drawText("Camera", mTextLeft, mTextTop, mPaint);
        mForegroundSelector.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = w;
        mLeft = (w - mLogoWidth) / 2;
        mTop = (h - mLogoWidth) / 2 - mTopOffset;
        mLogo.setBounds(mLeft, mTop, mLeft + mLogoWidth, mTop + mLogoWidth);
        int textWidth = (int) mPaint.measureText(mText, 0, mText.length());
        mTextLeft = (w - textWidth) / 2;
        int textHeight = (int) (mPaint.descent() - mPaint.ascent());
        mTextTop = (h - textHeight) / 2 + ScreenUtils.dp(getContext(), 25);
        mForegroundSelector.setBounds(0, 0, w, h);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try{
            setWillNotDraw(false);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e){
            DebugUtils.log("Error setting camera preview: " + e.getMessage());
            setBackgroundColor(Color.BLACK);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if(mHolder.getSurface() == null){
            return;
        }

        try{
            mCamera.stopPreview();
        }catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        try{
            setWillNotDraw(false);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e){
            DebugUtils.log("Error setting camera preview: " + e.getMessage());
            setBackgroundColor(Color.BLACK);
        }
    }

    private void setDisplayOrientation(){
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(0, info);
        int rotation = ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation){
            case Surface.ROTATION_0: degrees = 0;break;
            case Surface.ROTATION_90: degrees = 90;break;
            case Surface.ROTATION_180: degrees = 180;break;
            case Surface.ROTATION_270: degrees = 270;break;
        }

        int result;
        if(info.facing == CameraInfo.CAMERA_FACING_FRONT){
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        }else{
            result = (info.orientation - degrees + 360) % 360;
        }

        try {
            mCamera.setDisplayOrientation(result);
        }catch (RuntimeException e){
            e.printStackTrace();
            setBackgroundColor(Color.BLACK);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
}
