package me.yugy.qingbo.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.gif.GifDrawable;
import me.yugy.qingbo.view.image.ScaleImageView;

/**
 * Created by yugy on 2014/4/18.
 */
public class PicFragment extends Fragment implements LoaderManager.LoaderCallbacks<File>{

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

        getLoaderManager().initLoader(0, null, this);
//        ImageLoader.getInstance().loadImage(mPicUrl, new ImageSize(0, 0), OPTIONS, new SimpleImageLoadingListener(){
//            @Override
//            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                super.onLoadingComplete(imageUri, view, loadedImage);
//
//                if(mProgressBar.isShown()){
//                    mProgressBar.setProgress(100);
//                    mProgressBar.setVisibility(View.INVISIBLE);
//                }
//
//                File file = ImageLoader.getInstance().getDiscCache().get(imageUri);
//                try {
//                    if (TextUtils.isGifLink(mPicUrl)) {
//                        GifDrawable gifDrawable = new GifDrawable(file);
//                        mImageView.setImageDrawable(gifDrawable);
//                        gifDrawable.start();
//                    } else {
//                        mScaleImageView.setImageFile(file.getAbsolutePath());
//                    }
//                }catch (IOException e){
//                    e.printStackTrace();
//                    MessageUtils.toast(getActivity(), "IO error");
//                }
//            }
//        }, new ImageLoadingProgressListener() {
//            @Override
//            public void onProgressUpdate(String imageUri, View view, int current, int total) {
//                DebugUtils.log("Progress: " + current + ", total: " + total);
//                if(current == total){
//                    mProgressBar.setVisibility(View.INVISIBLE);
//                }else{
//                    if(!mProgressBar.isShown()){
//                        mProgressBar.setVisibility(View.VISIBLE);
//                    }
//                    mProgressBar.setProgress(current * 100 / total);
//                }
//            }
//        });
    }

    @Override
    public Loader<File> onCreateLoader(int id, Bundle args) {
        return new PicDownLoadTaskLoader(getActivity(), mPicUrl, new Handler()) {
            @Override
            public void publishProgress(int progress) {
                if(progress == 100){
                    mProgressBar.setProgress(progress);
                    mProgressBar.setVisibility(View.INVISIBLE);
                }else{
                    if(!mProgressBar.isShown()){
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setProgress(progress);
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<File> loader, File data) {
        try {
            if (TextUtils.isGifLink(mPicUrl)) {
                GifDrawable gifDrawable = new GifDrawable(data);
                mImageView.setImageDrawable(gifDrawable);
                gifDrawable.start();
            } else {
                mScaleImageView.setImageFile(data.getAbsolutePath());
            }
        }catch (IOException e){
            e.printStackTrace();
            MessageUtils.toast(getActivity(), "IO error");
        }
    }

    @Override
    public void onLoaderReset(Loader<File> loader) {

    }

    private abstract static class PicDownLoadTaskLoader extends AsyncTaskLoader<File>{

        private String mPicUrl;
        private Handler mHandler;
        private File mFile;

        public PicDownLoadTaskLoader(Context context, String picUrl, Handler handler) {
            super(context);
            mPicUrl = picUrl;
            mHandler = handler;
            String fileName = new HashCodeFileNameGenerator().generate(mPicUrl);
            File cacheDir = StorageUtils.getCacheDirectory(getContext());
            mFile = new File(cacheDir, fileName);
        }

        @Override
        protected void onStartLoading() {
            if(mFile.exists()){
                DebugUtils.log(mFile.getAbsolutePath() + " exists");
                onPublishProgress(100);
                deliverResult(mFile);
            }else{
                forceLoad();
            }
        }

        /**
         * update download progress
         * @param progress from 0 to 100
         */
        public abstract void publishProgress(int progress);

        private void onPublishProgress(final int progress){
            DebugUtils.log("update download progress: " + progress);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    publishProgress(progress);
                }
            });
        }

        private static final int RESULT_RESPONSE_CODE_ERROR = 0;
        private static final int RESULT_DOWNLOAD_FAILED = 1;
        private static final int RESULT_CLOSE_STREAM_FAILED = 2;

        /**
         * call when download is failed
         * @param result
         */
        private void onFailed(int result){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    MessageUtils.toast(getContext(), "Download failed");
                }
            });
        }

        @Override
        public File loadInBackground() {
            DebugUtils.log("start download: " + mPicUrl + " to " + mFile.getAbsolutePath());
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(mPicUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    onFailed(RESULT_RESPONSE_CODE_ERROR);
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(mFile);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button

                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        onPublishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                e.printStackTrace();
                onFailed(RESULT_DOWNLOAD_FAILED);
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                    onFailed(RESULT_CLOSE_STREAM_FAILED);
                }

                if (connection != null)
                    connection.disconnect();
            }
            return mFile;
        }
    }
}
