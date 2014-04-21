package me.yugy.qingbo.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;
import java.io.IOException;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.image.ScaleImageView;
import me.yugy.qingbo.view.gif.GifDrawable;

/**
 * Created by yugy on 2014/4/18.
 */
public class PicFragment extends Fragment{

    private static final DisplayImageOptions OPTIONS = new DisplayImageOptions.Builder()
            .cacheInMemory(false)
            .cacheOnDisc(true)
            .showImageOnFail(R.drawable.ic_image_fail)
            .displayer(new FadeInBitmapDisplayer(600))
            .build();

    private ScaleImageView mScaleImageView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private String mPicUrl;

    public PicFragment(String picUrl){
        mPicUrl = picUrl;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if(TextUtils.isGifLink(mPicUrl)){
            rootView = inflater.inflate(R.layout.fragment_gif, container, false);
            mImageView = (ImageView) rootView.findViewById(R.id.image);
        }else {
            rootView = inflater.inflate(R.layout.fragment_pic, container, false);
            mScaleImageView = (ScaleImageView) rootView.findViewById(R.id.image);
        }
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageLoader.getInstance().loadImage(mPicUrl, new ImageSize(0, 0), OPTIONS, new SimpleImageLoadingListener(){
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);

                if(mProgressBar.isShown()){
                    mProgressBar.setProgress(100);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }

                File file = ImageLoader.getInstance().getDiscCache().get(imageUri);
                try {
                    if (TextUtils.isGifLink(mPicUrl)) {
                        GifDrawable gifDrawable = new GifDrawable(file);
                        mImageView.setImageDrawable(gifDrawable);
                        gifDrawable.start();
                    } else {
                        mScaleImageView.setImageFile(file.getAbsolutePath());
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    MessageUtils.toast(getActivity(), "IO error");
                }
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                DebugUtils.log("Progress: " + current + ", total: " + total);
                if(current == total){
                    mProgressBar.setVisibility(View.INVISIBLE);
                }else{
                    if(!mProgressBar.isShown()){
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(current * 100 / total);
                }
            }
        });
    }
}
