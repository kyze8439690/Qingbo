package me.yugy.qingbo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.json.JSONObject;

import me.yugy.qingbo.R;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.fragment.ConvertLinkDialogFragment;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/5/25.
 */
public class NewStatusActivity extends Activity implements View.OnClickListener, TextWatcher {

    private RoundedImageView mHead;
    private TextView mName;
    private TextView mLimit;
    private EditText mEditText;
    private TextView mLocation;
    private ImageButton mPhoto;
    private ImageButton mMood;
    private ImageButton mLink;
    private ImageButton mSend;

    private static final DisplayImageOptions HEAD_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .showImageOnLoading(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .showImageForEmptyUri(R.drawable.default_head)
            .cacheInMemory(true)
            .cacheOnDisc(true)
            .displayer(new FadeInBitmapDisplayer(200))
            .build();
    private static final int MAX_COUNT = 140;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_status);

        //init views
        mHead = (RoundedImageView) findViewById(R.id.new_status_head);
        mName = (TextView) findViewById(R.id.new_status_name);
        mLimit = (TextView) findViewById(R.id.new_status_limit);
        mEditText = (EditText) findViewById(R.id.new_status_edittext);
        mLocation = (TextView) findViewById(R.id.new_status_location_txt);
        mPhoto = (ImageButton) findViewById(R.id.new_status_photo_btn);
        mMood = (ImageButton) findViewById(R.id.new_status_mood_btn);
        mLink = (ImageButton) findViewById(R.id.new_status_link_btn);
        mSend = (ImageButton) findViewById(R.id.new_status_send_btn);

        //load user info
        final long uid = Long.decode(PreferenceManager.getDefaultSharedPreferences(this).getString("uid", "-1"));
        UserInfo userInfo = new UserInfoDataHelper(this).select(uid);
        if(userInfo != null){
            mName.setText(userInfo.screenName);
            ImageLoader.getInstance().displayImage(userInfo.avatar,mHead, HEAD_OPTIONS);
        }else{
            mName.setText("Error");
        }

        //set edittext watcher
        mEditText.addTextChangedListener(this);

        //set button click listener
        mPhoto.setOnClickListener(this);
        mMood.setOnClickListener(this);
        mLink.setOnClickListener(this);
        mSend.setOnClickListener(this);

        //init send button state
        mSend.setEnabled(mEditText.length() > 0);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.new_status_photo_btn:

                break;
            case R.id.new_status_mood_btn:

                break;
            case R.id.new_status_link_btn:
                new ConvertLinkDialogFragment().show(getFragmentManager(), "convertLinkDialog");
                break;
            case R.id.new_status_send_btn:

                break;
        }
    }

    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * are about to be replaced by new text with length <code>after</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     *
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     * This method is called to notify you that, within <code>s</code>,
     * the <code>count</code> characters beginning at <code>start</code>
     * have just replaced old text that had length <code>before</code>.
     * It is an error to attempt to make changes to <code>s</code> from
     * this callback.
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * This method is called to notify you that, somewhere within
     * <code>s</code>, the text has been changed.
     * It is legitimate to make further changes to <code>s</code> from
     * this callback, but be careful not to get yourself into an infinite
     * loop, because any changes you make will cause this method to be
     * called again recursively.
     * (You are not told where the change took place because other
     * afterTextChanged() methods may already have made other changes
     * and invalidated the offsets.  But if you need to know here,
     * you can use Spannable.setSpan in {@link #onTextChanged}
     * to mark your place and then look up from here where the span
     * ended up.
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {

        //check text length and change send button state
        mSend.setEnabled(s.length() > 0);

        int editStart = mEditText.getSelectionStart();
        int editEnd = mEditText.getSelectionEnd();

        //remove the listener to prevent stackoverflow
        mEditText.removeTextChangedListener(this);

        //check and delete
        while(TextUtils.calculateTextLength(s.toString()) > MAX_COUNT){
            s.delete(editStart - 1, editEnd);
            editStart--;
            editEnd--;
        }

        mEditText.setSelection(editStart);

        //restore the listener
        mEditText.addTextChangedListener(this);

        //show the limit
        mLimit.setText(String.valueOf(MAX_COUNT - TextUtils.calculateTextLength(mEditText.getText().toString())));
    }

    @Override
    public void finish() {
        if(mEditText.length() > 0){
            new AlertDialog.Builder(this)
                    .setMessage("You have message hasn't been sent, are you sure you want to exit?")
                    .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            NewStatusActivity.super.finish();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }else {
            super.finish();
        }
    }
}
