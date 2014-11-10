package me.yugy.qingbo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.makeramen.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.ImageSizeUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import me.yugy.qingbo.R;
import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.fragment.ConvertLinkDialogFragment;
import me.yugy.qingbo.intent.NewStatusIntent;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.utils.ScreenUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.view.DragToDeleteRelativeLayout;
import me.yugy.qingbo.view.MentionEditText;
import me.yugy.qingbo.view.MentionListPopupWindow;

import static me.yugy.qingbo.fragment.ConvertLinkDialogFragment.OnConvertListener;

/**
 * Created by yugy on 2014/5/25.
 */
public class NewStatusActivity extends Activity implements View.OnClickListener, TextWatcher, OnConvertListener,
        MentionEditText.OnMentionListener, MentionListPopupWindow.OnMentionSelectListener, DragToDeleteRelativeLayout.OnSwipeToDeleteListener,
        AMapLocationListener{

    public static final int REQUEST_PICK_IMAGE = 773295428;
    public static final int REQUEST_EDIT_IMAGE = 88439690;

    private DragToDeleteRelativeLayout mDragToDeleteLayout;
    private RoundedImageView mHead;
    private TextView mName;
    private TextView mLimit;
    private MentionEditText mEditText;
    private TextView mLocation;
    private ImageButton mPhoto;
    private ImageButton mMood;
    private ImageButton mLink;
    private ImageButton mAt;
    private ImageView mThumbnail;
    private ImageButton mSend;
    private MentionListPopupWindow mMentionWindow;

    private LocationManagerProxy mLocationManagerProxy;

    private boolean mHasPicture = false;
    private boolean mHasLocation = false;
    private String mImagePath;
    private double mLatitude = -1;
    private double mLongitude = -1;

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
        mDragToDeleteLayout = (DragToDeleteRelativeLayout) findViewById(R.id.new_status_drag_layout);
        mHead = (RoundedImageView) findViewById(R.id.new_status_head);
        mName = (TextView) findViewById(R.id.new_status_name);
        mLimit = (TextView) findViewById(R.id.new_status_limit);
        mEditText = (MentionEditText) findViewById(R.id.new_status_edittext);
        mLocation = (TextView) findViewById(R.id.new_status_location_txt);
        mPhoto = (ImageButton) findViewById(R.id.new_status_photo_btn);
        mMood = (ImageButton) findViewById(R.id.new_status_mood_btn);
        mLink = (ImageButton) findViewById(R.id.new_status_link_btn);
        mAt = (ImageButton) findViewById(R.id.new_status_at_btn);
        mThumbnail = (ImageView) findViewById(R.id.new_status_thumbnail);
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

        mDragToDeleteLayout.setOnSwipeToDeleteListener(this);

        //set edittext watcher
        mEditText.addTextChangedListener(this);
        mEditText.setOnMentionListener(this);

        //set button click listener
        mPhoto.setOnClickListener(this);
        mMood.setOnClickListener(this);
        mLink.setOnClickListener(this);
        mAt.setOnClickListener(this);
        mSend.setOnClickListener(this);
        mLocation.setOnClickListener(this);
        mThumbnail.setOnClickListener(this);

        //init send button state
        mSend.setEnabled(mEditText.length() > 0);

        //check whether there is a photo
        if((mImagePath = getIntent().getStringExtra("imagePath")) != null){
            showThumbnail();
        }

        mLocationManagerProxy = LocationManagerProxy.getInstance(this);
        mLocationManagerProxy.setGpsEnable(false);

        if(getIntent().getBooleanExtra("getLocation", false)){
            locate();
        }
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
                if(mHasPicture){
                    showDeleteImageDialog();
                }else{
                    startActivityForResult(new Intent(this, PickPhotoSourceActivity.class), REQUEST_PICK_IMAGE);
                }
                break;
            case R.id.new_status_mood_btn:

                break;
            case R.id.new_status_link_btn:
                new ConvertLinkDialogFragment().show(getFragmentManager(), "convertLinkDialog");
                break;
            case R.id.new_status_at_btn:
                if(mEditText.length() == 0 || mEditText.getText().toString().endsWith(" ")){
                    mEditText.append("@");
                }else{
                    mEditText.append(" @");
                }
                break;
            case R.id.new_status_send_btn:
                NewStatusIntent.Builder builder = new NewStatusIntent.Builder(this)
                        .setText(mEditText.getText().toString())
                        .setLocation(mLatitude, mLongitude);
                if(mHasPicture){
                    builder.setImage(mImagePath);
                }
                startService(builder.create());
                finishWithoutNotify();
                break;
            case R.id.new_status_location_txt:
                if(mHasLocation){
                    removeLocation();
                }else{
                    locate();
                }
                break;
            case R.id.new_status_thumbnail:
                if(isAviaryInstalled()){
                    Intent intent = new Intent( "aviary.intent.action.EDIT" );
                    Uri uri = Uri.parse(mImagePath);
                    intent.setDataAndType(uri, "image/*"); // required
                    intent.putExtra("app-id", getPackageName()); // required ( it's your app unique package name )
                    startActivityForResult(intent, REQUEST_EDIT_IMAGE);
                }else{
                    new AlertDialog.Builder(this)
                            .setMessage("You have to install Aviary to edit the photo.Do you want to install it?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent( Intent.ACTION_VIEW );
                                    intent.setData( Uri.parse( "market://details?id=com.aviary.android.feather" ) );
                                    startActivity(intent);
                                }
                            }).setNegativeButton("Cancel", null)
                            .show();
                }
                break;
        }
    }

    private boolean isAviaryInstalled(){
        Intent intent = new Intent( "aviary.intent.action.EDIT" );
        intent.setType( "image/*" );
        List<ResolveInfo> list = getPackageManager()
                .queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY );
        return list.size() > 0;
    }

    private void locate(){
        mLocation.setText("Locating...");
        mLocationManagerProxy.requestLocationUpdates(LocationProviderProxy.AMapNetwork, 5000, 10, this);
        mLocation.setOnClickListener(null);
    }

    private void removeLocation(){
        mHasLocation = false;
        mLocation.setText("Add your location");
        mLatitude = -1;
        mLongitude = -1;
        mLocationManagerProxy.removeUpdates(this);
        if(mEditText.getText().toString().equals("我在这里")){
            mEditText.setText("");
        }
    }

    private void showDeleteImageDialog(){
        new AlertDialog.Builder(this)
                .setMessage("Delete existed image?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHasPicture = false;
                        mDragToDeleteLayout.delete();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static final DisplayImageOptions THUMBNAIL_OPTIONS = new DisplayImageOptions.Builder()
            .bitmapConfig(Bitmap.Config.RGB_565)
            .showImageOnLoading(R.drawable.ic_image_loading)
            .showImageOnFail(R.drawable.ic_image_fail)
            .considerExifParams(true)
            .build();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK){
            mImagePath = data.getStringExtra("imagePath");
            showThumbnail();
        }else if(requestCode == REQUEST_EDIT_IMAGE && resultCode == RESULT_OK){
            mImagePath = data.getData().toString();
            showThumbnail();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showThumbnail(){
        ImageLoader.getInstance().displayImage("file://" + mImagePath, mThumbnail, THUMBNAIL_OPTIONS);
        mDragToDeleteLayout.show();
        mHasPicture = true;
        if(mEditText.length() == 0){
            mEditText.setText("分享图片");
            mEditText.setSelection(0, mEditText.length());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

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
        if(mMentionWindow != null){
            //update existed mention list
            mMentionWindow.setKeyword(sequence.toLowerCase());
            if(!mMentionWindow.isShowing()){
                mMentionWindow.show();
            }
        }else{
            //show new mention list
            int textHeight = ScreenUtils.sp(this, 28);
            mMentionWindow = new MentionListPopupWindow(this, sequence.toLowerCase());
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
                        mMentionWindow.setKeyword("");
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void OnMentionFinished() {
        if(mMentionWindow != null && mMentionWindow.isShowing()){
            mMentionWindow.dismiss();
        }
    }

    @Override
    public void onMentionSelect(String name) {
        mEditText.setSelectedMention(name);
    }

    @Override
    public void onSwipeToDelete() {
        mHasPicture = false;
        if (mEditText.getText().toString().equals("分享图片")) {
            mEditText.setText("");
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        mHasLocation = true;
        mLocation.setOnClickListener(this);
        mLocation.setText(aMapLocation.getExtras().getString("desc"));
        mLatitude = aMapLocation.getLatitude();
        mLongitude = aMapLocation.getLongitude();
        if(mEditText.length() == 0){
            mEditText.setText("我在这里");
            mEditText.setSelection(0, mEditText.length());
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
