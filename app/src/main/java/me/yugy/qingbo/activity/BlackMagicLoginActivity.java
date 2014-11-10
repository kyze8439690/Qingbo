package me.yugy.qingbo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/4/30.
 */
public class BlackMagicLoginActivity extends Activity implements View.OnClickListener{

    private EditText mUserName;
    private EditText mPassword;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bm_login);

        mUserName = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mLogin = (Button) findViewById(R.id.login);
        mLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(mUserName.getText().length() == 0){
            mUserName.setError("Can not be empty");
            mUserName.requestFocus();
        }else if(mPassword.getText().length() == 0){
            mPassword.setError("Can not be empty");
            mPassword.requestFocus();
        }else{
            login();
        }
    }

    private void login(){
        final ProgressDialog progressDialog = ProgressDialog.show(
               BlackMagicLoginActivity.this,
                "授权中",
                "请稍等……",
                true,
                false
        );
        progressDialog.show();
        Weibo.getAccessToken(progressDialog.getContext(), mUserName.getText().toString(), mPassword.getText().toString(), new TextHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String uid) {
                Weibo.getUserInfo(progressDialog.getContext(), Long.decode(uid), new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        progressDialog.dismiss();
                        MessageUtils.toast(BlackMagicLoginActivity.this, "授权成功");
                        setResult(RESULT_OK);
                        finish();
                        super.onSuccess(statusCode, headers, response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        progressDialog.dismiss();
                        MessageUtils.toast(BlackMagicLoginActivity.this, responseString);
                        setResult(RESULT_CANCELED);
                        finish();
                        super.onFailure(statusCode, headers, responseString, throwable);
                    }
                });
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                progressDialog.dismiss();
                MessageUtils.toast(BlackMagicLoginActivity.this, responseBody);
                finish();
            }
        });
    }
}
