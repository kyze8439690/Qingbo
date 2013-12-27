package com.yugy.qingbo.ui.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yugy.qingbo.R;
import com.yugy.qingbo.Utils.MessageUtil;
import com.yugy.qingbo.Utils.ScreenUtil;
import com.yugy.qingbo.Utils.TextUtil;
import com.yugy.qingbo.ui.view.SlidingUpPanelLayout;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by yugy on 13-12-27.
 */
public class PicFragment extends Fragment implements PhotoViewAttacher.OnViewTapListener{

    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    private PhotoView mPhotoView;
    private ProgressBar mProgressBar;
    private static final DisplayImageOptions mOptions = new DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisc(true)
            .showImageForEmptyUri(R.drawable.ic_image_fail)
            .showImageOnFail(R.drawable.ic_image_fail)
            .displayer(new FadeInBitmapDisplayer(600))
            .build();
    private GifDrawable mGifDrawable = null;


    public PicFragment(SlidingUpPanelLayout slidingUpPanelLayout){
        mSlidingUpPanelLayout = slidingUpPanelLayout;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pic, container, false);
        mPhotoView = (PhotoView) rootView.findViewById(R.id.pic_photoview);
        mPhotoView.setOnViewTapListener(this);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.pic_progress);
        String picUrl = getArguments().getString("url");
        if(TextUtil.isGifLink(picUrl)){
            ImageLoader.getInstance().loadImage(picUrl, mOptions, new SimpleImageLoadingListener(){

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    mPhotoView.setImageResource(R.drawable.ic_image_fail);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    File file = ImageLoader.getInstance().getDiscCache().get(s);
                    try {
                        mGifDrawable = new GifDrawable(file);
                        mProgressBar.setVisibility(View.GONE);
                        mPhotoView.setImageDrawable(mGifDrawable);
                        mGifDrawable.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else{
            //display normal image
            ImageLoader.getInstance().displayImage(picUrl, mPhotoView, mOptions,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            mProgressBar.setVisibility(View.GONE);
                        }
                    },
                    new ImageLoadingProgressListener() {
                        @Override
                        public void onProgressUpdate(String s, View view, int i, int i2) {
                            MessageUtil.log(i + "/" + i2);
                        }
                    }
            );
        }
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if(mGifDrawable != null){
            mGifDrawable.recycle();
        }
        super.onDestroyView();
    }

    @Override
    public void onViewTap(View view, float v, float v2) {
        toggleActionBarAndPanel();
    }

    private void toggleActionBarAndPanel(){
        if (getActivity().getActionBar().isShowing()) {
            getActivity().getActionBar().hide();
            mSlidingUpPanelLayout.setPanelHeight(ScreenUtil.dp(getActivity(), 48));
        } else {
            getActivity().getActionBar().show();
            mSlidingUpPanelLayout.setPanelHeight(ScreenUtil.dp(getActivity(), 82 + 48));
        }
    }
}
