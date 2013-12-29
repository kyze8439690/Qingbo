package com.yugy.qingbo.ui.activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.yugy.qingbo.R;
import com.yugy.qingbo.utils.MessageUtils;
import com.yugy.qingbo.sdk.Weibo;
import com.yugy.qingbo.Conf;

import org.json.JSONObject;

public class WeiboLoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weibo_login);
        getActionBar().setDisplayShowHomeEnabled(false);
        initViews();
    }

    private WebView mWebView;

    private void initViews(){
        mWebView = (WebView) findViewById(R.id.weibo_login_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                MessageUtils.log("读取网址url: " + url);
                if(url.startsWith(Conf.WEIBO_CALLBACK_URL)){
                    view.loadUrl("about:blank");
                    Uri uri = Uri.parse(url);
                    String code = uri.getQueryParameter("code");
                    MessageUtils.log("新浪微博code: " + code);
                    final ProgressDialog progressDialog = ProgressDialog.show(
                            WeiboLoginActivity.this,
                            "授权中",
                            "请稍等……",
                            true,
                            false
                    );
                    progressDialog.show();
                    Weibo.getAccessToken(progressDialog.getContext(), code, new AsyncHttpResponseHandler(){
                        @Override
                        public void onSuccess(String content) {
                            Weibo.getUserInfo(progressDialog.getContext(), content, new JsonHttpResponseHandler(){
                                @Override
                                public void onSuccess(JSONObject response) {
                                    progressDialog.dismiss();
                                    MessageUtils.toast(WeiboLoginActivity.this, "授权成功");
                                    finish();
                                    super.onSuccess(response);
                                }

                                @Override
                                public void onFailure(Throwable e, String content) {
                                    progressDialog.dismiss();
                                    MessageUtils.toast(WeiboLoginActivity.this, content);
                                    finish();
                                    super.onFailure(e, content);
                                }
                            });
                            super.onSuccess(content);
                        }

                        @Override
                        public void onFailure(Throwable error, String content) {
                            progressDialog.dismiss();
                            MessageUtils.toast(WeiboLoginActivity.this, content);
                            finish();
                            super.onFailure(error, content);
                        }
                    });
                }else{
                    super.onPageStarted(view, url, favicon);
                }
            }
        });

        mWebView.loadUrl(Weibo.getAuthUrl());
    }


    
}
