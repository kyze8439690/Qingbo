package me.yugy.qingbo.vendor;

import android.content.Context;
import android.preference.PreferenceManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import me.yugy.qingbo.dao.datahelper.UserInfoDataHelper;
import me.yugy.qingbo.service.WeiboQueueService;
import me.yugy.qingbo.type.UserInfo;
import me.yugy.qingbo.utils.DebugUtils;

import static me.yugy.qingbo.service.WeiboQueueService.*;

/**
 * Created by yugy on 2014/3/29.
 */
public class Weibo {

    public static final String WEIBO_CALLBACK_URL = "http://qingbo.com";

    private final static String WEIBO_APP_KEY = "2314786277";
    private final static String WEIBO_APP_SECRET = "2f1eb03ab1dbb1e403bc60d6cb016edb";

    private final static String BM_WEIBO_APP_KEY = "211160679";
    private final static String BM_WEIBO_APP_SECRET = "63b64d531b98c2dbff2443816f274dd3";

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
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, "授权失败", e);
                super.onFailure(statusCode, headers, e, errorResponse);
            }

        });
    }

    /**
     * BlackMagic login
     * @param context
     * @param username
     * @param password
     * @param responseHandler
     */
    public static void getAccessToken(final Context context, String username, String password, final TextHttpResponseHandler responseHandler){
        RequestParams params = new RequestParams();
        params.put("client_id", BM_WEIBO_APP_KEY);
        params.put("client_secret", BM_WEIBO_APP_SECRET);
        params.put("grant_type", "password");
        params.put("username", username);
        params.put("password", password);
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
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, "授权失败", e);
                super.onFailure(statusCode, headers, e, errorResponse);
            }

        });
    }

    public static void getUserInfo(final Context context, long userId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("uid", String.valueOf(userId));
        mClient.get(context, WeiboApiUrl.USER_SHOW, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log("获取用户信息成功: " + response.toString());
                UserInfoDataHelper dataHelper = new UserInfoDataHelper(context);
                UserInfo userInfo = new UserInfo();
                try {
                    userInfo.parse(response);
                    dataHelper.insert(userInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responseHandler.onSuccess(statusCode, headers, response);
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, "获取用户信息失败", throwable);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });

    }

    public static void getUserInfo(final Context context, String userName, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("screen_name", userName);
        mClient.get(context, WeiboApiUrl.USER_SHOW, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log("获取用户信息成功: " + response.toString());
                UserInfoDataHelper dataHelper = new UserInfoDataHelper(context);
                UserInfo userInfo = new UserInfo();
                try {
                    userInfo.parse(response);
                    dataHelper.insert(userInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responseHandler.onSuccess(statusCode, headers, response);
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, "获取用户信息失败", throwable);
                super.onFailure(statusCode, headers, throwable, errorResponse);
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
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response.toString());
                try {
                    responseHandler.onSuccess(statusCode, headers, response.getJSONArray("statuses"));
                } catch (JSONException e) {
                    responseHandler.onFailure(statusCode, headers, "获取Timeline失败", e);
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
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
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response.toString());
                try {
                    responseHandler.onSuccess(statusCode, headers, response.getJSONArray("statuses"));
                } catch (JSONException e) {
                    responseHandler.onFailure(statusCode, headers, "获取用户信息失败", e);
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static void getNewComments(Context context, long statusId, long sinceId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("id", String.valueOf(statusId));
        params.put("since_id", String.valueOf(sinceId));
        DebugUtils.log("newestCommentId:" + sinceId);
        mClient.get(context, WeiboApiUrl.COMMENTS_SHOW, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response.toString());
                responseHandler.onSuccess(statusCode, headers, response);
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static void getOldComments(Context context, long statusId, long lastCommentId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("id", String.valueOf(statusId));
        params.put("max_id", String.valueOf(lastCommentId));
        DebugUtils.log("oldestCommentId:" + lastCommentId);
        mClient.get(context, WeiboApiUrl.COMMENTS_SHOW, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response.toString());
                responseHandler.onSuccess(statusCode, headers, response);
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable,  errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static void createComment(Context context, String comment, long statusId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("comment", comment);
        params.put("id", String.valueOf(statusId));
        DebugUtils.log("Comment: " + comment);
        mClient.post(context, WeiboApiUrl.COMMENTS_CREATE, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response.toString());
                responseHandler.onSuccess(statusCode, headers, response);
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static void createFriendShip(Context context, String userName, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("screen_name", userName);
        mClient.post(context, WeiboApiUrl.FRIENDSHIPS_CREATE, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response.toString());
                responseHandler.onSuccess(statusCode, headers, response);
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static void destroyFriendShip(Context context, String userName, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("screen_name", userName);
        mClient.post(context, WeiboApiUrl.FRIENDSHIPS_DESTROY, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response.toString());
                responseHandler.onSuccess(statusCode, headers, response);
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static void getUserNewTimeline(Context context, String userName, long sinceId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("screen_name", userName);
        params.put("since_id", String.valueOf(sinceId));
        DebugUtils.log("SinceId: " + sinceId);
        mClient.get(context, WeiboApiUrl.STATUS_USER_TIMELINE, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response);
                try {
                    responseHandler.onSuccess(statusCode, headers, response.getJSONArray("statuses"));
                } catch (JSONException e) {
                    responseHandler.onFailure(statusCode, headers, "获取Timeline失败", e);
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static void getFriends(final Context context, final JsonHttpResponseHandler responseHandler){
        final JSONArray result = new JSONArray();
        final long uid = Long.decode(PreferenceManager.getDefaultSharedPreferences(context).getString("uid", "-1"));
        RequestParams params = getParamsWithAccessToken(context);
        params.put("count", "200");
        params.put("trim_status", "1");
        params.put("uid", String.valueOf(uid));
        mClient.get(context, WeiboApiUrl.FRIENDSHIPS_FRIENDS, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response);
                try {
                    JSONArray jsonArray = response.getJSONArray("users");
                    int length = jsonArray.length();
                    for (int i = 0; i < length; i++) {
                        result.put(jsonArray.getJSONObject(i));
                    }
                    int cursor;
                    if((cursor = response.getInt("next_cursor")) != 0){
                        getFriends(context, uid, cursor, this);
                    }else{
                        responseHandler.onSuccess(statusCode, headers, result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }
        });
    }

    private static void getFriends(Context context, long uid, int nextCursor, JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("count", "200");
        params.put("cursor", String.valueOf(nextCursor));
        params.put("trim_status", "1");
        params.put("uid", String.valueOf(uid));
        mClient.get(context, WeiboApiUrl.FRIENDSHIPS_FRIENDS, params, responseHandler);
    }

    public static void getUserOldTimeline(Context context, String userName, long maxId, final JsonHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("screen_name", userName);
        params.put("max_id", String.valueOf(maxId));
        DebugUtils.log("MaxId: " + maxId);
        mClient.get(context, WeiboApiUrl.STATUS_USER_TIMELINE, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response);
                try {
                    responseHandler.onSuccess(statusCode, headers, response.getJSONArray("statuses"));
                } catch (JSONException e) {
                    responseHandler.onFailure(statusCode, headers, "获取Timeline失败", e);
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse);
                responseHandler.onFailure(statusCode, headers, throwable, errorResponse);
                super.onFailure(statusCode, headers, throwable, errorResponse);
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
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response.toString());
                responseHandler.onSuccess(statusCode, headers, response);
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                DebugUtils.log(errorResponse.toString());
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static void getShortUrl(Context context, String longUrl, final TextHttpResponseHandler responseHandler){
        RequestParams params = getParamsWithAccessToken(context);
        params.put("url_long", longUrl);
        mClient.get(context, WeiboApiUrl.SHORTURL_SHORTEN, params, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                DebugUtils.log(response);
                try {
                    responseHandler.onSuccess(statusCode, headers, response.getJSONArray("urls").getJSONObject(0).getString("url_short"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                super.onSuccess(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                responseHandler.onFailure(statusCode, headers, errorResponse.toString(), throwable);
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public static JSONObject syncNewStatusOnlyText(Context context, String status, double latitude, double longitude) throws IOException {
        try {
            URL url = new URL(WeiboApiUrl.STATUS_UPDATE);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            RequestParams params = getParamsWithAccessToken(context);
            params.put("status", URLEncoder.encode(status, "utf-8"));
            if(latitude != -1 && longitude != -1) {
                params.put("lat", String.valueOf(latitude));
                params.put("long", String.valueOf(longitude));
            }

            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            out.writeBytes(params.toString());
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer result = new StringBuffer();

            while((inputLine = in.readLine()) != null){
                result.append(inputLine);
            }
            in.close();

            DebugUtils.log(result.toString());

            try {
                JSONObject json = new JSONObject(result.toString());
                return json;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JSONObject syncNewStatusWithImage(Context context, String status, String imagePath,
                                                    double latitude, double longitude,
                                                    OnProgressListener onProgressListener) throws IOException {
        try {
            String twoHyphens = "--";
            String boundary =  "*****kyze8439690*****";
            String lineEnd = "\r\n";

            int bytesRead, bytesAvailable, bufferSize, bytesWrite;
            byte[] buffer;
            int maxBufferSize = 10 * 1024;

            File file = new File(imagePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            URL url = new URL(WeiboApiUrl.STATUS_UPLOAD);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            StringBuilder headBuilder = new StringBuilder();
            if(latitude != -1 && longitude != -1){
                headBuilder.append(twoHyphens + boundary + lineEnd);
                headBuilder.append("Content-Disposition: form-data; name=\"lat\"" + lineEnd + lineEnd);
                headBuilder.append(URLEncoder.encode(String.valueOf(latitude), "utf-8") + lineEnd);

                headBuilder.append(twoHyphens + boundary + lineEnd);
                headBuilder.append("Content-Disposition: form-data; name=\"long\"" + lineEnd + lineEnd);
                headBuilder.append(URLEncoder.encode(String.valueOf(longitude), "utf-8") + lineEnd);
            }

            headBuilder.append(twoHyphens + boundary + lineEnd);
            headBuilder.append("Content-Disposition: form-data; name=\"access_token\"" + lineEnd + lineEnd);
            headBuilder.append(URLEncoder.encode(getAccessToken(context), "utf-8") + lineEnd);

            headBuilder.append(twoHyphens + boundary + lineEnd);
            headBuilder.append("Content-Disposition: form-data; name=\"status\"" + lineEnd + lineEnd);
            headBuilder.append(URLEncoder.encode(status, "utf-8") + lineEnd);

            headBuilder.append(twoHyphens + boundary + lineEnd);
            headBuilder.append("Content-Disposition: form-data; name=\"pic\"; filename=\"" + file.getName() +"\"" + lineEnd);
            headBuilder.append("Content-Type: image/jpeg" + lineEnd + lineEnd);

            StringBuilder tailBuilder = new StringBuilder(lineEnd + lineEnd);
            tailBuilder.append(twoHyphens + boundary + twoHyphens + lineEnd);

            long length = headBuilder.length() + file.length() + tailBuilder.length();
            conn.setRequestProperty("Content-length", String.valueOf(length));
            conn.setFixedLengthStreamingMode((int)length);

            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());

            outputStream.writeBytes(headBuilder.toString());

            bytesWrite = 0;
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            //write file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while(bytesRead > 0) {
                bytesWrite += bytesRead;
                outputStream.write(buffer, 0, bufferSize);
                int progress = (int) (bytesWrite * 100 / file.length());
                onProgressListener.onProgressChange(progress);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(tailBuilder.toString());

            outputStream.flush();
            outputStream.close();

            onProgressListener.onProgressChange(100);

            //read response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer result = new StringBuffer();

            while((inputLine = in.readLine()) != null){
                result.append(inputLine);
            }
            in.close();

            DebugUtils.log(result.toString());

            try {
                JSONObject json = new JSONObject(result.toString());
                return json;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static RequestParams getParamsWithAccessToken(Context context){
        RequestParams params = new RequestParams();
        params.put("access_token", getAccessToken(context));
        return params;
    }

    private static String getAccessToken(Context context){
        if(mAccessToken == null){
            mAccessToken = PreferenceManager.getDefaultSharedPreferences(context).getString("access_token", null);
        }
        return mAccessToken;
    }

    public final static class WeiboApiUrl{
        public static final String STATUS_HOME_TIMELINE = "https://api.weibo.com/2/statuses/home_timeline.json";
        public static final String STATUS_USER_TIMELINE = "https://api.weibo.com/2/statuses/user_timeline.json";
        public static final String STATUS_UPDATE = "https://api.weibo.com/2/statuses/update.json";
        public static final String STATUS_UPLOAD = "https://upload.api.weibo.com/2/statuses/upload.json";
        public static final String USER_SHOW = "https://api.weibo.com/2/users/show.json";
        public static final String OAUTH2_ACCESS_TOKEN = "https://api.weibo.com/oauth2/access_token";
        public static final String OAUTH2_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";
        public static final String COMMENTS_SHOW = "https://api.weibo.com/2/comments/show.json";
        public static final String COMMENTS_CREATE = "https://api.weibo.com/2/comments/create.json";
        public static final String REPOSTS_SHOW = "https://api.weibo.com/2/statuses/repost_timeline.json";
        public static final String FRIENDSHIPS_CREATE = "https://api.weibo.com/2/friendships/create.json";
        public static final String FRIENDSHIPS_DESTROY = "https://api.weibo.com/2/friendships/destroy.json";
        public static final String FRIENDSHIPS_FRIENDS = "https://api.weibo.com/2/friendships/friends.json";
        public static final String SHORTURL_SHORTEN = "https://api.weibo.com/2/short_url/shorten.json";
    }

}
