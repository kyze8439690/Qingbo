package me.yugy.qingbo.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import me.yugy.qingbo.view.image.SubsamplingScaleImageView;

/**
 * Created by yugy on 2014/4/18.
 */
public class PicFragment extends Fragment implements LoaderManager.LoaderCallbacks<File>, View.OnClickListener {

    private SubsamplingScaleImageView mScaleImageView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private String mPicUrl;

    private int mRotateOrientation = 0;

    public static PicFragment getInstance(String picUrl){
        PicFragment picFragment = new PicFragment();
        Bundle argument = new Bundle();
        argument.putString("picUrl", picUrl);
        picFragment.setArguments(argument);
        return picFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPicUrl = getArguments().getString("picUrl");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if(TextUtils.isGifLink(mPicUrl)){
            rootView = inflater.inflate(R.layout.fragment_gif, container, false);
            mImageView = (ImageView) rootView.findViewById(R.id.image);
            mImageView.setOnClickListener(this);
        }else {
            rootView = inflater.inflate(R.layout.fragment_pic, container, false);
            mScaleImageView = (SubsamplingScaleImageView) rootView.findViewById(R.id.image);
            mScaleImageView.setOnClickListener(this);
            setHasOptionsMenu(true);
        }
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.pic, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.rotate){
            if(mScaleImageView != null) {
                if (mRotateOrientation == 270) {
                    mRotateOrientation = 0;
                } else {
                    mRotateOrientation += 90;
                }
                mScaleImageView.setOrientation(mRotateOrientation);
            }
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public void onClick(View v) {
        getActivity().finish();
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
