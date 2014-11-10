package me.yugy.qingbo.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.view.CameraPreviewView;

/**
 * Created by yugy on 2014/6/4.
 */
public class PickPhotoSourceActivity extends Activity implements View.OnClickListener{

    public static final int REQUEST_PICK_IMAGE_FROM_GALLERY = 523483845;
    public static final int REQUEST_PICK_IMAGE_FROM_CAMERA = 694525726;

    private Camera mCamera;
    private CameraPreviewView mCameraPreviewView;
    private Button mGallery;

    private String mImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_photo_source);

        mCameraPreviewView = (CameraPreviewView) findViewById(R.id.photo_source_camera);
        mGallery = (Button) findViewById(R.id.photo_source_gallery);

        mCameraPreviewView.setOnClickListener(this);
        mGallery.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera = getCaremaInstance();
        mCameraPreviewView.init(mCamera);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.photo_source_camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        mImagePath = photoFile.getAbsolutePath();
                    } catch (IOException e) {
                        e.printStackTrace();
                        MessageUtils.toast(this, "Create image file failed.");
                    }
                    if(photoFile != null){
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(intent, REQUEST_PICK_IMAGE_FROM_CAMERA);
                    }
                }
                break;
            case R.id.photo_source_gallery:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_PICK_IMAGE_FROM_GALLERY);
                }
                break;
        }
    }

    public File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    public String getImagePathFromUri(Uri uri){
        Cursor cursor = getContentResolver().query(uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(0);
        cursor.close();
        return imagePath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_PICK_IMAGE_FROM_GALLERY && resultCode == RESULT_OK){
            if(data != null && data.getData() != null){
                mImagePath = getImagePathFromUri(data.getData());
                pickSuccess();
            }else{
                MessageUtils.toast(this, "Pick photo from gallery failed.");
            }
        }else if(requestCode == REQUEST_PICK_IMAGE_FROM_CAMERA && resultCode == RESULT_OK){
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            File f = new File(mImagePath);
            Uri contentUri = Uri.fromFile(f);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            pickSuccess();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void pickSuccess(){
        Intent result = new Intent();
        result.putExtra("imagePath", mImagePath);
        setResult(RESULT_OK, result);
        finish();
    }

    private Camera getCaremaInstance(){
        Camera c = null;
        try{
            c = Camera.open();
        }catch (Exception e){
            e.printStackTrace();
            MessageUtils.toast(this, "Camera is not available (in use or does not exist)");
        }
        return c;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

}
