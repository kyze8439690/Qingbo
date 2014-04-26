package me.yugy.qingbo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import me.yugy.qingbo.R;
import me.yugy.qingbo.utils.DebugUtils;
import me.yugy.qingbo.utils.MessageUtils;
import me.yugy.qingbo.vendor.Weibo;

/**
 * Created by yugy on 2014/4/16.
 */
public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_login);
        WebView webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    setProgressBarIndeterminateVisibility(false);
                } else {
                    setProgressBarIndeterminateVisibility(true);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                DebugUtils.log("读取网址url: " + url);
                if (url.startsWith(Weibo.WEIBO_CALLBACK_URL)) {
                    view.loadUrl("about:blank");
                    Uri uri = Uri.parse(url);
                    String code = uri.getQueryParameter("code");
                    DebugUtils.log("新浪微博code: " + code);
                    final ProgressDialog progressDialog = ProgressDialog.show(
                            LoginActivity.this,
                            "授权中",
                            "请稍等……",
                            true,
                            false
                    );
                    progressDialog.show();
                    Weibo.getAccessToken(progressDialog.getContext(), code, new TextHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String uid) {
                            Weibo.getUserInfo(progressDialog.getContext(), Long.decode(uid), new JsonHttpResponseHandler() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    progressDialog.dismiss();
                                    MessageUtils.toast(LoginActivity.this, "授权成功");
                                    setResult(RESULT_OK);
                                    finish();
                                    super.onSuccess(response);
                                }

                                @Override
                                public void onFailure(Throwable e, String content) {
                                    progressDialog.dismiss();
                                    MessageUtils.toast(LoginActivity.this, content);
                                    setResult(RESULT_CANCELED);
                                    finish();
                                    super.onFailure(e, content);
                                }
                            });
                            super.onSuccess(statusCode, headers, uid);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                            progressDialog.dismiss();
                            MessageUtils.toast(LoginActivity.this, responseBody);
                            finish();
                            super.onFailure(statusCode, headers, responseBody, error);
                        }
                    });
                } else {
                    super.onPageStarted(view, url, favicon);
                }
            }
        });

        webView.loadUrl(Weibo.getAuthUrl());
    }
}
