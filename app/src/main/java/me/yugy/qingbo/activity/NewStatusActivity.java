package me.yugy.qingbo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import me.yugy.qingbo.R;
import me.yugy.qingbo.adapter.TestAdapter;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.fragment.ConvertLinkDialogFragment;
import me.yugy.qingbo.intent.NewStatusIntent;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.ScreenUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.MentionEditText;
import me.yugy.qingbo.view.MentionListPopupWindow;

import static me.yugy.qingbo.fragment.ConvertLinkDialogFragment.OnConvertListener;

/**
 * Created by yugy on 2014/5/25.
 */
public class NewStatusActivity extends Activity implements View.OnClickListener, TextWatcher, OnConvertListener,
        MentionEditText.OnMentionListener, MentionListPopupWindow.OnMentionSelectListener {

    private RoundedImageView mHead;
    private TextView mName;
    private TextView mLimit;
    private MentionEditText mEditText;
    private TextView mLocation;
    private ImageButton mPhoto;
    private ImageButton mMood;
    private ImageButton mLink;
    private ImageButton mSend;
    private MentionListPopupWindow mMentionWindow;

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
        mEditText = (MentionEditText) findViewById(R.id.new_status_edittext);
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
        mEditText.setOnMentionListener(this);

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
                NewStatusIntent intent = new NewStatusIntent.Builder(this)
                        .setText(mEditText.getText().toString())
                        .create();
                startService(intent);
                finishWithoutNotify();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

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

    public void finishWithoutNotify(){
        super.finish();
    }

    /**
     * call when link convert is successful.
     * @param shortUrl the converted short url.
     */
    @Override
    public void onUrlConvertSuccess(String shortUrl) {
        if(mEditText.length() == 0 || mEditText.getText().charAt(mEditText.length() - 1) == ' '){
            mEditText.append(shortUrl + " ");
        }else{
            mEditText.append(" " + shortUrl + " ");
        }

    }

    /**
     * call when link convert is failed.
     * @param originUrl the original url which fail to be converted.
     */
    @Override
    public void onUrlConvertFailure(String originUrl) {
        ConvertLinkDialogFragment convertLinkDialogFragment = new ConvertLinkDialogFragment();
        Bundle argument = new Bundle();
        argument.putBoolean("invalid url", true);
        argument.putString("url", originUrl);
        convertLinkDialogFragment.setArguments(argument);
        convertLinkDialogFragment.show(getFragmentManager(), "convertLinkDialog");
    }

    @Override
    public void OnMentionStarted(String sequence) {
        if(mMentionWindow != null && mMentionWindow.isShowing()){
            //update existed mention list
            mMentionWindow.setKeyword(sequence);
        }else{
            //show new mention list
            int textHeight = ScreenUtils.sp(this, 28);
            mMentionWindow = new MentionListPopupWindow(this, sequence);
            mMentionWindow.setAnchorView(findViewById(R.id.new_status_anchor));
            mMentionWindow.setVerticalOffset(mEditText.getCurrentLineTop() + textHeight);
            mMentionWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    DebugUtils.log("listPopupWindow dismiss.");
                }
            });
            mMentionWindow.setOnMentionSelectListener(this);
            mMentionWindow.show();
            mEditText.setOnBackPressedListener(new MentionEditText.OnBackPressedListener() {
                @Override
                public boolean onBackPressed() {
                    if(mMentionWindow.isShowing()){
                        mMentionWindow.dismiss();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void OnMentionFinished() {
        DebugUtils.log("Mention finished.");
        if(mMentionWindow != null && mMentionWindow.isShowing()){
            mMentionWindow.dismiss();
        }
    }

    @Override
    public void onMentionSelect(String name) {
        mEditText.setSelectedMention(name);
    }
}
