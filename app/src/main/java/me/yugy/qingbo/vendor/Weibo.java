package me.yugy.qingbo.vendor;

import android.content.Context;
import android.preference.PreferenceManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.utils.DebugUtils;

/**
 * Created by yugy on 2014/3/29.
 */
public class Weibo {

    public static final String WEIBO_CALLBACK_URL = "http://qingbo.com";
    private final static String WEIBO_APP_KEY = "2314786277";
    private final static String WEIBO_APP_SECRET = "2f1eb03ab1dbb1e403bc60d6cb016edb";

    private static String mAccessToken = null;


    private static AsyncHttpClient mClient = new AsyncHttpClient();

    public static String getAuthUrl(){
        return WeiboApiUrl.OAUTH2_AUTHORIZE + "?client_id="+ WEIBO_APP_KEY
                +"&response_type=code&display=mobile&redirect_uri=" + WEIBO_CALLBACK_URL;
    }

    /**
     * 根据code获取accessToken，若获取成功则存入数据库，并在onSuccess(String)中返回userId
     * @param context
     * @param code
     * @param responseHandler
     */
    public static void getAccessToken(final Context context, String code, final TextHttpResponseHandler responseHandler){
        RequestParams params = new RequestParams();
        params.put("client_id", WEIBO_APP_KEY);
        params.put("client_secret", WEIBO_APP_SECRET);
        params.put("grant_type", "authorization_code");
        params.put("code", code);
        params.put("redirect_uri", WEIBO_CALLBACK_URL);
        mClient.post(context, WeiboApiUrl.OAUTH2_ACCESS_TOKEN, params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log("授权成功: " + response.toString());
                try {
                    mAccessToken = response.getString("access_token");
                    String uid = response.getString("uid");
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                           .putString("access_token", mAccessToken)
                           .putString("uid", uid)
                           .commit();
                    responseHandler.onSuccess(statusCode, headers, uid);
                } catch (JSONException e) {
                    e.printStackTrace();
                    responseHandler.onFailure(statusCode, headers, "Json解析失败", e);
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                DebugUtils.log("授权失败: " + errorResponse.toString());
                responseHandler.onFailure(statusCode, headers, "授权失败", e);
                super.onFailure(statusCode, headers, e, errorResponse);
            }

        });
    }

    public static void getUserInfo(final Context context, String userId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("uid", userId);
        mClient.get(context, WeiboApiUrl.USER_SHOW, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                DebugUtils.log("获取用户信息成功: " + response.toString());
                UserInfoDataHelper dataHelper = new UserInfoDataHelper(context);
                UserInfo userInfo = new UserInfo();
                try {
                    userInfo.parse(response);
                    dataHelper.insert(userInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responseHandler.onSuccess(response);
                super.onSuccess(response);
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                DebugUtils.log("获取用户信息失败: " + errorResponse.toString());

                responseHandler.onFailure(e, "获取用户信息失败");
                super.onFailure(e, errorResponse);
            }
        });

    }

    public static void getNewTimeline(Context context, long firstStatusId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("since_id", String.valueOf(firstStatusId));
        params.put("count", String.valueOf(20));
        DebugUtils.log("firstStatusId: " + firstStatusId);
        mClient.get(context, WeiboApiUrl.STATUS_HOME_TIMELINE, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                DebugUtils.log(response.toString());
                try {
                    responseHandler.onSuccess(response.getJSONArray("statuses"));
                } catch (JSONException e) {
                    responseHandler.onFailure(e, "获取Timeline失败");
                    e.printStackTrace();
                }
                super.onSuccess(response);
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                DebugUtils.log(errorResponse.toString());
                super.onFailure(e, errorResponse);
            }
        });
    }

    public static void getOldTimeline(Context context, long lastStatusId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("max_id", String.valueOf(lastStatusId));
        params.put("count", String.valueOf(21));
        DebugUtils.log("firstStatusId: " + lastStatusId);
        mClient.get(context, WeiboApiUrl.STATUS_HOME_TIMELINE, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject response) {
                DebugUtils.log(response.toString());
                try {
                    responseHandler.onSuccess(response.getJSONArray("statuses"));
                } catch (JSONException e) {
                    responseHandler.onFailure(e, "获取用户信息失败");
                    e.printStackTrace();
                }
                super.onSuccess(response);
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                DebugUtils.log(errorResponse.toString());
                super.onFailure(e, errorResponse);
            }
        });
    }

    public static void getComments(Context context, String statusId, String sinceId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("id", statusId);
        params.put("since_id", sinceId);
        params.put("count", "20");
        mClient.get(context, WeiboApiUrl.COMMENTS_SHOW, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject response) {
                DebugUtils.log(response.toString());
                responseHandler.onSuccess(response);
                super.onSuccess(response);
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                DebugUtils.log(errorResponse.toString());
                super.onFailure(e, errorResponse);
            }
        });
    }

    public static void getReposts(Context context, String statusId, String sinceId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("id", statusId);
        params.put("since_id", sinceId);
        params.put("count", "20");
        mClient.get(context, WeiboApiUrl.REPOSTS_SHOW, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(JSONObject response) {
                DebugUtils.log(response.toString());
                responseHandler.onSuccess(response);
                super.onSuccess(response);
            }

            @Override
            public void onFailure(Throwable e, JSONObject errorResponse) {
                DebugUtils.log(errorResponse.toString());
                super.onFailure(e, errorResponse);
            }
        });
    }

    private static RequestParams getParamsWithAccessToken(Context context){
        if(mAccessToken == null){
            mAccessToken = PreferenceManager.getDefaultSharedPreferences(context).getString("access_token", null);
        }
        RequestParams params = new RequestParams();
        params.put("access_token", mAccessToken);
        return params;
    }

    public final static class WeiboApiUrl{
        public static final String STATUS_HOME_TIMELINE = "https://api.weibo.com/2/statuses/home_timeline.json";
        public static final String USER_SHOW = "https://api.weibo.com/2/users/show.json";
        public static final String OAUTH2_ACCESS_TOKEN = "https://api.weibo.com/oauth2/access_token";
        public static final String OAUTH2_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";
        public static final String COMMENTS_SHOW = "https://api.weibo.com/2/comments/show.json";
        public static final String REPOSTS_SHOW = "https://api.weibo.com/2/statuses/repost_timeline.json";
    }

}
