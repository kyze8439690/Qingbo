package me.yugy.qingbo.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.TextUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/5/25.
 */
public class ConvertLinkDialogFragment extends DialogFragment implements View.OnClickListener, DialogInterface.OnClickListener{

    private EditText mEditText;
    private ImageButton mClear;

    private OnConvertListener mOnConvertListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_link_dialog, null);
        mEditText = (EditText) view.findViewById(R.id.link_dialog_edittext);
        mClear = (ImageButton) view.findViewById(R.id.link_dialog_clear_btn);
        mClear.setOnClickListener(this);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton("OK", this)
                .setNegativeButton("Cancel", null)
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try{
            mOnConvertListener = (OnConvertListener) getActivity();
        }catch (ClassCastException e){
            throw new ClassCastException("Activity must implement the OnConvertListener interface");
        }

        //restore from error
        if(getArguments() != null){
            if(getArguments().getBoolean("invalid url")){
                mEditText.setText(getArguments().getString("url", "http://"));
                mEditText.setError("The provided url is not valid");
                mEditText.setSelection(0, mEditText.length());
            }else{
                //normal boot up
                getUrlfromClipboard();
            }
        }else{
            //normal boot up
            getUrlfromClipboard();
        }
    }

    private void getUrlfromClipboard(){
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboardManager.hasPrimaryClip()){
            if(clipboardManager.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
                CharSequence text = clipboardManager.getPrimaryClip().getItemAt(0).getText();
                DebugUtils.log("has text in clipboard: " + text);
                if(TextUtils.isUrl(text.toString())){
                    mEditText.setText(text);
                }
            }
        }
        mEditText.setSelection(mEditText.length());
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        mEditText.setText("");
    }

    /**
     * This method will be invoked when a button in the dialog is clicked.
     *
     * @param dialog The dialog that received the click.
     * @param which  The button that was clicked (e.g.
     *               {@link android.content.DialogInterface#BUTTON1}) or the position
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        final String url = mEditText.getText().toString();
        final ProgressDialog progressDialog = new ProgressDialog(getActivity(), ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Converting link...");
        dismiss();
        progressDialog.show();
        Weibo.getShortUrl(getActivity(), url, new TextHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseBody) {
                progressDialog.dismiss();
                mOnConvertListener.onUrlConvertSuccess(responseBody);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //not valid url
                progressDialog.dismiss();
                mOnConvertListener.onUrlConvertFailure(url);
            }
        });
    }

    public static interface OnConvertListener{
        /**
         * call when link convert is successful.
         * @param shortUrl the converted short url.
         */
        public void onUrlConvertSuccess(String shortUrl);

        /**
         * call when link convert is failed.
         * @param originUrl the original url which fail to be converted.
         */
        public void onUrlConvertFailure(String originUrl);
    }
}
